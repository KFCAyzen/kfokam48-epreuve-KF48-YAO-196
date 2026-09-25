# Présence et relecture entre pairs — KFOKAM48

Épreuve finale fullstack KFOKAM48 · KEPSEU Franck Celestin · KF48-YAO-196

Un formateur ouvre une session et affiche un code de présence ; les étudiants le saisissent et déposent le lien de leur exercice. Chaque exercice est relu par **deux pairs** tirés au hasard ; la note retenue est la moyenne des deux, **provisoire** tant qu'un seul a rendu. Le formateur suit présences, dépôts, moyennes et relectures en attente dans un tableau.

- **Backend :** Java 21, Spring Boot 4.1, Maven (wrapper `mvnw` inclus), PostgreSQL 16, migrations Flyway.
- **Frontend :** React. Choisi parce que trois écrans de formulaires et de tableaux n'ont besoin ni de rendu serveur ni d'un framework complet : React avec Vite donne le code le plus court et un build statique simple à servir.

## Démarrer

Prérequis : **Docker** avec Docker Compose. Rien d'autre à installer.

```bash
git clone https://github.com/KFCAyzen/kfokam48-epreuve-KF48-YAO-196.git
cd kfokam48-epreuve-KF48-YAO-196
docker compose up --build
```

Au premier lancement, la construction des images prend quelques minutes. Ensuite :

| Adresse | Contenu |
|---|---|
| http://localhost:3000 | L'application : écrans formateur, étudiant et relecteur |
| http://localhost:8080/api/promotions | L'API, par exemple la liste des promotions |

Pour tout arrêter : `docker compose down`. Pour repartir d'une base vide : `docker compose down -v`.

Si le port 8080 ou 3000 est déjà pris sur ton poste, choisis-en d'autres au lancement :

```bash
API_PORT=18080 FRONT_PORT=13000 docker compose up --build
```

### Données de démonstration

Chargées automatiquement au premier démarrage, si la base est vide. Elles reprennent les données de référence des maquettes (`docs/maquettes/README.md`) :

- promotion **« 2026-A · Développement logiciel »** : 12 étudiants, six sessions ;
  - S1 à S5, les jours précédents, clôturées, avec leurs présences (dont des ajouts du formateur) et des exercices relus chacun par deux pairs ; en S5, deux exercices n'ont qu'une relecture rendue : leur note est **provisoire** ;
  - S6, **ouverte au démarrage** : son code, affiché sur l'écran formateur, est valable 15 minutes pour essayer le marquage de présence ; trois exercices y attendent leurs deux relecteurs ;
- promotion « 2026-B · Data et IA » : 4 étudiants, aucune session.

Pour essayer : écran **Formateur** → choisir la promotion 2026-A → le code de S6 et l'onglet « Tableau de la promotion » ; écran **Étudiant** → se désigner (par exemple « Tagne, Joël », absent de S6) → saisir le code ; écran **Relecteur** → « Djomo, Hervé » a une relecture à commencer ; écran **Étudiant** → « Wamba, Loïc » voit dans « Mes exercices » une note marquée provisoire.

Après 15 minutes le code de S6 a expiré (`410 CODE_EXPIRE`) : ouvrez une nouvelle session depuis l'écran formateur, ou repartez de zéro avec `docker compose down -v` puis `docker compose up`.

## Ce que livre la v1.0

| Écran | Fonctionnalités |
|---|---|
| Formateur | ouvrir une session et projeter son code ; sessions de la promotion ; tableau par étudiant (présences, exercices, moyenne, provisoire ou non, relectures en attente) |
| Étudiant | se désigner sans mot de passe ; marquer sa présence ; déposer son exercice ; « Mes exercices » avec la note retenue |
| Relecteur | relectures assignées ; commencer, puis rendre une note entière sur 20 et un commentaire |

Hors du périmètre de la v1.0 (sortis pour absorber la double relecture, voir `CHANGELOG.md` et le cahier des charges, section 10) : blocage après 5 codes faux, présence ajoutée à la main, clôture d'une session, remplacement du lien, détail d'une session, grille de présence.

## Tests

Les tests tournent sur un poste vierge, **sans base de données installée** : ils démarrent eux-mêmes un PostgreSQL 16 jetable dans un conteneur (Testcontainers), la même base que l'application, avec les mêmes migrations Flyway. Il faut Docker et un JDK 21.

```bash
cd backend
./mvnw verify
```

Sous Windows, remplacer `./mvnw` par `mvnw.cmd`.

Tests du front (Vitest, sans navigateur ni backend) :

```bash
cd frontend
npm ci
npm test
```

## Organisation du dépôt

| Dossier | Contenu |
|---|---|
| `docs/` | `CAHIER_DES_CHARGES.md`, `JOURNAL.md`, `diagrammes/` (Mermaid), `maquettes/` (spécifications des écrans) |
| `api/` | `contrat.yaml`, le contrat OpenAPI |
| `backend/` | L'API Spring Boot : `controller`, `service`, `repository`, `entity`, `dto`, `exception` ; migrations dans `src/main/resources/db/migration` |
| `frontend/` | L'application React ; tous les appels réseau sont dans `src/api/` |
| `docker-compose.yml` | PostgreSQL, API et front |

## Façon de travailler

- Une issue par résultat attendu, priorisée Must / Should / Could, qui cite ses exigences `EFx` et ses règles `RGx`.
- Une branche par issue, créée depuis `develop` ; une pull request par branche ; le dernier commit ferme l'issue avec `Closes #n`.
- `main` et `develop` sont protégées : aucune écriture directe, tout passe par une pull request.
- `develop` est publiée dans `main` à chaque jalon : `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`.
