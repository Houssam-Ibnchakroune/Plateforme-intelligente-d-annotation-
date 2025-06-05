# 📚 Annonateur

Application web **Spring Boot / Thymeleaf** pour la gestion et l’annotation collaborative de jeux de paires de phrases (NLI, similarité, etc.).

> **Encadrant :** Pr. **Tarik Boudaa**  
> **Contributeurs :** **Ibnchakroune Houssam** · **Kamal Salma**

---

## ✨ Fonctionnalités

| Bloc | Ce que l’on peut faire |
|------|------------------------|
| **Authentification** | Connexion / déconnexion sécurisée via Spring Security. |
| **Administration** | • Créer / modifier des annotateurs <br> • désactiver (suppression logique) |
| **Datasets** | • Import CSV / JSON (`id,text1,text2`) <br> • Aperçu des 5 premières paires <br> • Affectation d’annotateurs <br> • Calcul de l’avancement (%) |
| **Annotation** | Interface minimale où chaque annotateur étiquette ses paires restantes. |
| **Export** | CSV final : `id,texte,classe,annotateur,date`. |
| **Analyse (Python)** | • `metrics.py` → Fleiss κ global + κ par annotateur <br> • `spam.py` → détection des “spammeurs” (κ \< seuil). Résultats intégrés côté Web. |

---

## 🏗️ Architecture rapide

- `src/main/java`
  - `controllers`     ← couche web
  - `services`        ← logique métier (import, export, métriques …)
  - `models`          ← entités JPA
  - `repositories`    ← interfaces Spring Data
  - `security`        ← configuration Spring Security

- `src/main/resources/templates`
  - …                ← vues Thymeleaf (`.html`)

- `scripts`
  - `metrics.py`
  - `spam.py`


---

## ⚙️ Prérequis

| Outil | Version conseillée |
|-------|--------------------|
| JDK   | 21 + |
| Maven | 4.0 + |
| Python| ≥ 3.12 (avec **numpy**, **pandas**, **scikit-learn**) |
| MySQL | 8 + (ou tout SGDB compatible JDBC) |

---

## 🚀 Installation & exécution

```bash
# 1. cloner le projet
git clone https://github.com/Houssam-Ibnchakroune/Plateforme-intelligente-d-annotation-.git
cd ./Plateforme-intelligente-d-annotation-/Annonateur

# 2. (optionnel) créer un venv Python puis installer les libs
python -m venv .venv && source .venv/bin/activate
pip install -r scripts/requirements.txt  # numpy, pandas, scikit-learn

# 3. configurer la BDD (src/main/resources/application.properties)
#    spring.datasource.url, username, password

# 4. lancer
mvn spring-boot:run
```
Par défaut, les scripts Python sont cherchés dans ./scripts.

## 🔑 Comptes de démo
Rôle	Login / mot de passe
Admin	admin / admin123
Annot.	annot1 / annotpass



## 🖱️ Parcours typique
Connexion en tant qu’admin (/login).

Gestion Annotateurs : CRUD + bouton Désactiver (suppression logique : on passe enabled=false, les stats sont conservées).

Onglet Datasets :

Créer → formulaire (nom, description, fichier CSV/JSON).

Affecter → bouton Affecter → coche les annotateurs.

Détails → aperçus, % d’avancement, export, métriques, spammeurs.

Annotation (profil annotateur) : liste de paires non étiquetées, choix du label, sauvegarde.

Export final → CSV signé (id, texte, classe , annotateur, date).

## 📝 Notes d’implémentation
Point	Détail
Suppression “logique”	On ne supprime jamais vraiment : on garde les historiques d’annotations.

Taux d’avancement	completionPercent = #paires_annotées / #total • 100.

Requêtes custom dans TextPairRepository.

Fleiss κ	Calculé en Python (plus simple pour le matriciel) puis renvoyé en JSON.

Détection spam	κ individuel vs majorité < seuil (0.20) et au moins n ≥ 3 paires.

Sécurité	Spring Security basique : formulaire login, BCrypt + rôle ADMIN / ANNOTATOR.
