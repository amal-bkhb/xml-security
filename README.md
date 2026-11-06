<div align="center">

# 🔏 XML Security Project — Échange de données XML sécurisé

**Modélisation, validation, signature et chiffrement de documents XML métier (facturation, commandes, paiements) avec espaces de noms, XSD/DTD et Apache Santuario.**

Projet académique — ENSIAS, filière Cybersecurity, Cloud & Mobile Computing — 2025/2026

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white)
![XML](https://img.shields.io/badge/XML-XSD%20%2F%20DTD-orange)
![Santuario](https://img.shields.io/badge/Apache-Santuario%20xmlsec-D22128?logo=apache&logoColor=white)
![Licence](https://img.shields.io/badge/licence-MIT-green)

</div>

> Projet d'échange de données XML d'entreprise : un même jeu de documents métier
> (clients, commandes, factures, paiements, tickets) est modélisé, validé par schémas,
> puis sécurisé par **signature** et **chiffrement** XML.

---

## Objectifs

- Modéliser des documents XML métier cohérents et reliés entre eux par des références.
- Valider chaque document à l'aide de **schémas XSD et DTD**.
- Gérer les **espaces de noms** XML, y compris un cas multi-namespaces.
- Appliquer les standards **XML Signature** et **XML Encryption** avec Apache Santuario.

## Contenu du dépôt

```
xml-security-project/
├── xml-data/      # documents XML métier (données d'exemple fictives)
├── schemas/       # schémas de validation XSD et DTD
├── security/      # démo Java de signature et chiffrement XML
└── pom.xml        # configuration Maven (dépendance org.apache.santuario:xmlsec)
```

### Documents XML (`xml-data/`)

| Fichier | Contenu | Schéma |
|---------|---------|--------|
| `clients.xml` | Référentiel clients | `clients.xsd` / `clients.dtd` |
| `commandes.xml` | Commandes | `commandes.xsd` / `commandes.dtd` |
| `factures.xml` | Factures | `factures.xsd` / `factures.dtd` |
| `paiements.xml` | Paiements (référencent les factures) | `paiements.xsd` / `paiements.dtd` |
| `tickets.xml` | Tickets de support | `tickets.xsd` / `tickets.dtd` |
| `echanges-multi-ns.xml` | Démonstration multi-espaces de noms | `echanges-multi-ns.xsd` |
| `exemple-signe.xml` | Exemple de document signé | `exemple-signe.xsd` |
| `exemple-chiffre.xml` | Exemple de document chiffré | `exemple-chiffre.xsd` |

Les données sont **fictives** et servent uniquement de démonstration.

## Partie sécurité (`security/XmlSecurityDemo.java`)

La démo illustre deux standards du W3C via Apache Santuario :

- **Signature XML (enveloped)** : signature RSA-SHA256 de `factures.xml`, avec canonicalisation
  exclusive, empreinte SHA-256 et insertion de la clé publique dans le `KeyInfo`.
- **Chiffrement XML** : chiffrement d'un élément `facture` en **AES-256**, la clé AES étant
  elle-même protégée par **RSA-OAEP** (chiffrement hybride, pattern standard de XML Encryption).

Les clés (paire RSA 2048 bits, clé AES 256 bits) sont **générées à l'exécution** ; aucune clé
ni aucun secret n'est stocké dans le dépôt.

## Compilation et exécution

Prérequis : JDK 17 et Maven.

```bash
mvn compile
mvn exec:java -Dexec.mainClass=security.XmlSecurityDemo
```

La démo lit `xml-data/factures.xml` et produit `factures-signees.xml` et
`factures-chiffrees.xml` (ces fichiers générés sont ignorés par Git).

## Validation des documents

Chaque document peut être validé contre son schéma avec un outil XML (xmllint, par exemple) :

```bash
xmllint --noout --schema schemas/factures.xsd xml-data/factures.xml
```



## Licence

Distribué sous licence [MIT](LICENSE).
