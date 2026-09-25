# Journal de bord — KF48-YAO-196

> Une entrée **par étape**, écrite **au moment où tu la termines**, pas à la fin de la journée.
> Trois lignes suffisent. Un journal rédigé d'un bloc juste avant de soumettre se repère
> immédiatement dans l'historique Git et ne compte pas.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que tu viens de terminer
- **Bloqué** — ce qui t'a coûté du temps, et combien
- **IA** — ce que tu lui as demandé, et **comment tu as vérifié sa réponse**

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges (14 exigences fonctionnelles, 9 non fonctionnelles, 24 règles de gestion, 17 zones d'ombre, 1 contradiction franche et 4 tensions tranchées), les quatre diagrammes en Mermaid (D4 en bonus), 19 issues créées (16 stories Must/Should/Could, la CI, le contrat et la clôture d'étape), contrat d'API complété (5 opérations imposées intactes, 11 ajoutées), `develop` créée et `main`/`develop` protégées, commit `[JALON] analyse` poussé.

**Bloqué :** 20 min d'environnement : seul Java 8 était installé, j'ai installé le JDK 21 ; et mon premier commit poussé contenait une ligne d'attribution à l'IA, j'ai préféré supprimer et recréer le dépôt plutôt que de réécrire `main`. 10 min sur la contradiction Q10 / Q15, tranchée en faveur de Q15 : le contrat imposé renvoie `409 RELECTURE_DEJA_RENDUE`, une note rendue ne peut donc pas être corrigée. 15 min sur la lisibilité de D1 et D4.

**IA :** Claude (Claude Code) a rédigé le cahier des charges, les diagrammes, les issues et les ajouts au contrat à partir de mes consignes. Vérifié ainsi :
- **cahier des charges :** comparé titre par titre et colonne par colonne avec `modeles/CAHIER_DES_CHARGES.md`. Sa première version ne respectait pas la numérotation du modèle (EF1 = présence, RG1 à RG3) : réalignée dans un commit dédié ;
- **diagrammes :** rendus dans un navigateur avec Mermaid 11 (4/4 sans erreur) et relus sur capture. Deux «extend» de D1 étaient dans le mauvais sens : corrigés avant commit ;
- **contrat :** validé comme OpenAPI 3.0, puis un script a comparé les 5 opérations imposées avec l'original : aucun corps, paramètre ni code retiré. J'ai aussi vérifié que chaque issue n'utilise que des opérations du contrat.

---

## Étape 2 — Première version

**Fait :** v0.1 livrée par une branche et une pull request par issue, chaque issue fermée par le dernier commit de sa branche (`Closes #n`). Les sept stories Must (EF1 à EF7 : #3 à #9), le socle (#1 démarrage par `docker compose` avec données de démonstration, #2 format d'erreur, #17 CI obligatoire sur `develop` et `main`, #46 tests du front) et #23 (analyse mise à jour après les précisions de l'examinateur). 82 tests backend (unitaires sur RG1, RG2, RG3, RG5, RG11, RG14, RG15, RG19 ; intégration sur chaque code des cinq opérations imposées) et 28 tests front. Écrans conformes aux spécifications `docs/maquettes/` et au design handoff. Vérifié de bout en bout sur PostgreSQL avec `docker compose` : codes 201, 400, 403, 404, 409 et 410 conformes au contrat, tableau en 87 ms.

**Bloqué :** 30 min d'environnement : disque saturé par les images Docker, Docker Desktop arrêté, et port 8080 déjà pris par un autre projet ; j'ai rendu les ports réglables (`API_PORT`, `FRONT_PORT`). 15 min sur Figma : le quota MCP du plan Starter était atteint, donc les écarts des maquettes ont été tranchés dans le code, à partir des spécifications versionnées dans `docs/maquettes/`. 10 min sur deux commits du front qui ne compilaient pas seuls ; je les ai refaits avant de pousser. Le test de push de LISEZ-MOI (`chore: verification du depot`, anciennement `[JALON] depart`) n'a pas été fait sous cette forme : mon premier push, le `.gitignore`, a servi de vérification.

**IA :** Claude (Claude Code) a écrit le code backend et frontend et ses tests, issue par issue. Vérifié ainsi :
- **règles et codes :** chaque règle de gestion est prouvée par un test qui la nomme, et chaque code du contrat par un test d'intégration ;
- **défaut trouvé :** un test a révélé que Jackson tronquait une note 12.5 en 12 ; c'est corrigé en refusant les décimales ;
- **front :** composants relus avec la grille react-best-practices ; chaque commit du front compilé isolément ; parcours complet rejoué à la main contre `docker compose` ;
- **CI :** obligatoire avant chaque fusion.

---

## Étape 3 — Enveloppe

**Fait :**
- **Bug.** Issue #56 ouverte à 15:15, avant tout code. Le symptôme du client (deux présences simultanées, une seule enregistrée) venait de RG15 : les deux transactions assignaient le même exercice en attente, et la seconde violait la clé de `relecture`, ce qui annulait sa présence. Test `PresencesSimulteesTest` poussé **en échec** (`0d2ad9f`), puis correction par verrou de session (`e402316`, « Closes #56 »), et le test passe. Branche et PR dédiées (#57).
- **Changement de besoin (deux relecteurs, note provisoire).** Analyse mise à jour avant le code (#58, PR #62) : cahier v2, D1 à D4 et contrat, dans des commits qui disent que c'est une conséquence du changement. Puis l'API (#59, PR #65), avec la **nouvelle migration V2** (V1 intacte) et un test qui prouve qu'une base remplie en V1 survit à V2. Puis les écrans (#60, PR #66).
- **À ta demande, H2 retiré** (#63, PR #64) : les tests tournent maintenant sur PostgreSQL 16 par Testcontainers.

**Bloqué :**
- 15 min : une autre session travaillant dans le même dossier a changé de branche sous mes pieds, et trois commits d'analyse sont partis sur `docs/61`. Je les ai récupérés sur `docs/58` par un simple push, sans réécriture.
- 10 min : le passage à PostgreSQL a fait échouer 18 tests. La cause n'était pas la base mais des classes recompilées par l'éditeur sans l'option `-parameters` ; un `mvnw clean` a suffi.

**IA :** Claude (Claude Code) a proposé la traduction du bug et écrit tests et code. Vérifié ainsi :
- **bug :** le test a d'abord échoué avec l'erreur exacte attendue (violation de clé sur `relecture(exercice_id)`), et il passe après la correction ;
- **migration :** testée sur une vraie base PostgreSQL, en migrant d'abord jusqu'à V1 seulement, puis vers V2 ;
- **contrat :** un script compare les opérations imposées à l'original après chaque modification.

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :** la double relecture est un Must qui arrive tard et touche la base, le contrat et trois écrans. Pour la livrer testée avant 18h00, six stories quittent la v1.0 : #10 (blocage après 5 codes faux), #11 (présence ajoutée à la main), #12 (clôture), #13 (remplacement du lien), #15 (détail d'une session) et #16 (grille de présence). Aucune n'est nécessaire au changement, et chacune a un repli acceptable : le code expire en 15 minutes, le tableau montre les relectures en attente, le dépôt reste ouvert sans clôture. Chaque issue porte un commentaire qui le dit et le label « hors-périmètre ». À l'inverse, #14 (voir sa note) monte en Must : sans elle, l'étudiant ne verrait pas sa note provisoire.

---

## Étape 4 — Version finale

**Fait :** CHANGELOG (analyse, v0.1, v1.0, avec les issues et PR de l’historique), README v1.0, backlog trié (seules restent ouvertes les six stories hors périmètre, sans jalon ; #61 fermée avec explication). README suivi depuis un clone vierge de `develop` : `docker compose up --build` construit et démarre les trois services ; la base déjà remplie en V1 a survécu à la migration V2 sur PostgreSQL. Commit `[JALON] v1.0`, publié sur `main`.

**Bloqué :** 60 min : le disque plein a ralenti la construction des images Docker ; clone refait dans un chemin court (limite de 260 caractères de Windows) ; port 8080 déjà pris, test mené sur les ports de repli du README.

**IA :** Claude a rédigé le CHANGELOG et le README à partir de la liste réelle des PR fusionnées (`gh pr list`) ; vérifié en recoupant chaque numéro d’issue et de PR avec l’historique, et en suivant le README pas à pas sur un clone vierge.

---

## Étape 5 — Soumission

**Fait :** v1.0 publiée sur `main` (PR #71), avec les trois jalons dans l'ordre : `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`. Cette entrée est publiée avant de relever le hash déclaré sur la plateforme. Dépôt public vérifié sans être connecté. `SOUMISSION.md` rempli à partir du modèle ; la partie « Épreuve Git » est sans objet, l'examinateur ayant supprimé cette épreuve à midi. Délai prolongé d'une heure par l'examinateur ; je l'ai mis à profit pour ajouter une documentation Swagger de l'API (#75) : générée par le backend (springdoc, vérifiée par un test sur les cinq opérations imposées) et Swagger UI du contrat dans `docker compose` (vérifié en le lançant). Le hash déclaré a été relevé après cette dernière publication.

**Ce que je referais autrement avec une journée de plus :**

1. **Des opérations CRUD complètes.** Aujourd'hui, les promotions et les étudiants viennent des données de démonstration, et une session ou un exercice ne se modifie ni ne se supprime. Je donnerais au formateur de quoi créer, lire, modifier et supprimer :
   - ses promotions et leurs étudiants, y compris l'import d'une liste ;
   - ses sessions : corriger le titre, clôturer (EF10) ;
   - les exercices : remplacer le lien (EF11).

   Chaque opération aurait ses règles de gestion : pas de suppression d'une session qui a déjà des présences ou des exercices, pas de suppression d'un étudiant qui a des relectures. Ses codes d'erreur seraient ajoutés au contrat, et ses tests d'intégration écrits comme pour les cinq opérations imposées.
2. **Une meilleure organisation et implémentation des interfaces.**
   - Une bibliothèque de composants communs (formulaires, tableaux, cartes, messages d'état), alignée sur la maquette, au lieu de styles repris écran par écran.
   - Une navigation plus claire entre les rôles, et un écran formateur complet : détail d'une session (EF13) et grille de présence (EF14).
   - Des états de chargement, d'erreur et « vide » identiques partout, et une accessibilité vérifiée (clavier, contrastes).
   - Des tests de bout en bout dans un vrai navigateur (Playwright) sur les parcours des trois rôles.
3. **Une meilleure sécurité.** L'identité est aujourd'hui déclarative (Q1) : n'importe qui peut choisir le nom d'un autre ou envoyer un autre `X-Etudiant-Id`. J'ajouterais :
   - une vraie authentification avec Spring Security (comptes, mots de passe hachés, ou connexion par lien envoyé par e-mail) et des jetons de session ;
   - des rôles formateur et étudiant contrôlés par l'API, et non par l'écran ;
   - le blocage après 5 codes faux (RG7) et une limitation du débit des requêtes ;
   - une politique CORS et des en-têtes de sécurité HTTP ;
   - les identifiants de la base hors du dépôt (variables d'environnement) ;
   - une journalisation des actions sensibles (ajout de présence, relecture rendue).

Aussi :
- **Méthode de travail :** travailler dans un seul dossier par session et libérer de l'espace disque avant de commencer ; une session parallèle et le disque plein m'ont coûté plus d'une heure.
- **Robustesse :** nommer explicitement les paramètres des contrôleurs (`@PathVariable("id")`), pour ne plus dépendre de l'option `-parameters` du compilateur.
