package security;

// Dépendance requise: org.apache.santuario:xmlsec (voir pom.xml)
import org.apache.xml.security.Init;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Démo simple Apache Santuario:
 * 1) signature XML enveloped
 * 2) chiffrement XML d'un élément
 */
public class XmlSecurityDemo {

    public static void main(String[] args) throws Exception {
        Init.init();

        Path input = Path.of("xml-data/factures.xml");
        Path signedOutput = Path.of("xml-data/factures-signees.xml");
        Path encryptedOutput = Path.of("xml-data/factures-chiffrees.xml");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        Document signedDoc = parseXml(input);
        signXml(signedDoc, kp.getPrivate(), kp.getPublic());
        writeXml(signedDoc, signedOutput);

        Document encryptedDoc = parseXml(input);
        encryptFirstInvoice(encryptedDoc, kp.getPublic());
        writeXml(encryptedDoc, encryptedOutput);

        System.out.println("Signature et chiffrement terminés.");
    }

    private static Document parseXml(Path file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(file.toFile());
    }

    private static void signXml(Document doc, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        Element root = doc.getDocumentElement();
        XMLSignature signature = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
        root.appendChild(signature.getElement());

        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        signature.addDocument("", transforms, org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA256);
        signature.addKeyInfo(publicKey);
        signature.sign(privateKey);
    }

    private static void encryptFirstInvoice(Document doc, PublicKey publicKey) throws Exception {
        Element root = doc.getDocumentElement();
        Element target = (Element) root.getElementsByTagNameNS("http://entreprise.com/nsInvoice", "facture").item(0);

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey aesKey = kg.generateKey();

        XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
        keyCipher.init(XMLCipher.WRAP_MODE, publicKey);

        XMLCipher dataCipher = XMLCipher.getInstance(XMLCipher.AES_256);
        dataCipher.init(XMLCipher.ENCRYPT_MODE, aesKey);

        EncryptedData encryptedData = dataCipher.getEncryptedData();
        KeyInfo keyInfo = new KeyInfo(doc);
        keyInfo.add(keyCipher.encryptKey(doc, aesKey));
        encryptedData.setKeyInfo(keyInfo);

        dataCipher.doFinal(doc, target, false);
    }

    private static void writeXml(Document doc, Path output) throws Exception {
        javax.xml.transform.Transformer transformer =
                javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");

        try (var os = Files.newOutputStream(output)) {
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(os));
        }
    }
}
