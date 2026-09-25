# Cahier des charges — Présence & relecture entre pairs KFOKAM48

**Auteur :** KEPSEU Franck Celestin · KF48-YAO-196
**Version :** 1 · **Date :** 25/09/2026
**Frontend choisi :** React (Vite + TypeScript), parce que trois écrans de formulaires et de tableaux n'ont besoin ni de rendu serveur (Next.js) ni d'un framework complet (Angular) : React donne le code le plus court et un build statique simple à servir par nginx dans `docker compose`.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 suit aujourd'hui la présence en cours et les exercices des étudiants à la main. Deux problèmes en découlent :

- **la présence n'est pas fiable** : un appel papier se contourne, et rien ne permet d'éviter qu'un étudiant absent soit déclaré présent par un camarade ;
- **les exercices ne sont pas relus** : le formateur ne peut pas corriger seul chaque exercice de chaque session.

L'application répond aux deux avec un seul outil. Le formateur ouvre une session de cours et affiche un **code de présence à durée courte**. Les étudiants présents dans la salle le saisissent. Chaque étudiant dépose le **lien** de son exercice. Le système confie chaque exercice à **un pair présent**, tiré au hasard, qui le note et le commente. Le formateur suit l'ensemble dans un **tableau par étudiant** : présences, exercices déposés, moyenne des notes reçues, relectures encore dues.

L'objectif est qu'en fin de séance le formateur sache, sans rien ressaisir, qui était là, qui a rendu son travail, quelle note il a reçue et qui n'a pas fait sa relecture.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session et obtenir son code (EF1) · ajouter une présence à la main (EF9) · clôturer une session (EF10) · consulter les sessions et les exercices en attente (EF13) · consulter le tableau de sa promotion (EF7, EF14) | Relire ou noter un exercice · modifier une note · supprimer une présence · choisir le relecteur |
| **Étudiant** | Se désigner dans la liste de sa promotion (EF2) · marquer sa présence avec le code (EF3) · déposer le lien de son exercice (EF4) et le remplacer (EF11) · voir la note et le commentaire reçus (EF12) | Marquer sa présence après expiration du code ou deux fois · déposer deux exercices pour la même session · choisir son relecteur · connaître le nom de son relecteur · relire son propre exercice |
| **Relecteur** | Voir les exercices qui lui sont assignés · commencer une relecture, ce qui lui révèle le lien · rendre une note et un commentaire (EF6) | Relire son propre exercice · relire un exercice qui ne lui est pas assigné · modifier une relecture rendue |
| **Système** (acteur secondaire) | Générer le code de présence · tirer le relecteur au hasard (EF5) · bloquer un étudiant après 5 erreurs de code (EF8) · calculer la moyenne | — |

**Le relecteur n'est pas un acteur distinct : c'est un étudiant, dans le rôle que lui donne une relecture qui lui a été assignée.** Un même étudiant est auteur de son exercice et relecteur de celui d'un pair lors de la même session. Conséquence sur le modèle de données : il n'y a pas de table `relecteur` ; la table `relecture` porte une clé étrangère `relecteur_id` vers `etudiant` (voir D2).

Le formateur n'a pas de compte (Q1) : il n'est pas représenté en base, l'écran formateur est accessible sans identification (voir Z10).

## 3. Périmètre

**Inclus dans cette version :**
- ouverture d'une session de cours pour une promotion, avec un code de présence valable 15 minutes ;
- marquage de la présence par l'étudiant avec le code, et ajout manuel par le formateur, tracé comme tel ;
- protection contre la devinette de codes : blocage de 2 minutes après 5 erreurs ;
- dépôt du lien d'un exercice par session, et son remplacement tant que la relecture n'a pas commencé ;
- attribution automatique et aléatoire d'un relecteur parmi les étudiants présents ;
- relecture : note entière sur 20 et commentaire, définitive une fois rendue ;
- consultation par l'étudiant de sa note et du commentaire, sans le nom du relecteur ;
- clôture d'une session par le formateur ;
- tableau récapitulatif par étudiant pour une promotion ;
- identification sans mot de passe, par choix dans une liste ;
- données de démonstration chargées au démarrage, lancement par `docker compose up`.

**Explicitement exclu :**
- authentification, mots de passe, rôles et comptes (Q1) — l'identité est déclarative ;
- création, modification et suppression des promotions, des étudiants et des formateurs : elles viennent des données de démonstration ;
- correction d'une note déjà rendue (Q15 retenue contre Q10, voir C1) ;
- réattribution manuelle d'un relecteur par le formateur, ou désistement d'un relecteur ;
- vérification que le lien déposé est accessible ou pointe vers un vrai dépôt : seule la forme de l'URL est contrôlée ;
- notifications (e-mail, SMS, push) ;
- export du tableau (CSV, PDF) et statistiques au-delà des quatre indicateurs de Q16 ;
- historique ou audit des modifications ;
- mode hors ligne, application mobile native, interface multilingue ;
- réouverture d'une session clôturée.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session pour une promotion et obtient un code de présence | Quand je saisis un titre, choisis une promotion et valide, alors un code de 6 caractères s'affiche avec son heure d'expiration, égale à l'heure d'ouverture + 15 min, et `POST /api/sessions` a répondu `201 { id, code, ouvertureAt, expirationAt }`. Quand le titre manque, alors je reçois `400 CHAMP_MANQUANT` | Must |
| EF2 | L'utilisateur se désigne en choisissant sa promotion puis son nom dans une liste, sans mot de passe | Quand j'ouvre l'écran étudiant ou relecteur, alors je choisis ma promotion puis mon nom dans des listes fournies par l'API ; mon choix est conservé jusqu'à ce que je clique « Changer d'identité » | Must |
| EF3 | L'étudiant marque sa présence en saisissant le code | Quand je saisis le code d'une session de ma promotion dans les 15 min, alors je reçois `201` avec `source = ETUDIANT` et la colonne « présences » du tableau augmente de 1. Code inexistant : `400 CODE_INCONNU`. Code expiré : `410 CODE_EXPIRE`. Déjà présent : `409 DEJA_PRESENT` | Must |
| EF4 | L'étudiant dépose le lien de son exercice pour une session de sa promotion | Quand je choisis une session non clôturée et saisis une URL `http(s)://…`, alors je reçois `201 { id, statut }` et « exercices déposés » augmente de 1 dans le tableau. Second dépôt pour la même session : `409 EXERCICE_DEJA_DEPOSE`. URL invalide : `400 LIEN_INVALIDE` | Must |
| EF5 | Le système assigne automatiquement à chaque exercice un relecteur tiré au hasard parmi les étudiants présents à la session, jamais l'auteur | Quand un exercice est déposé et qu'au moins un autre étudiant est présent, alors son statut est `EN_ATTENTE_RELECTURE` et il apparaît sur l'écran relecteur d'un étudiant présent différent de l'auteur. Quand aucun étudiant éligible n'est présent, alors il reste `DEPOSE`, puis est assigné dès qu'un étudiant éligible marque sa présence | Must |
| EF6 | Le relecteur commence la relecture d'un exercice assigné, puis rend une note et un commentaire | Quand je clique « Commencer », alors le lien de l'exercice s'affiche. Quand j'envoie la note 14 et un commentaire, alors je reçois `200` et l'exercice passe `RELU`. Note 12.5 ou 21 : `400 NOTE_INVALIDE`. Second envoi : `409 RELECTURE_DEJA_RENDUE`. L'auteur qui tente de relire son exercice : `403 AUTO_RELECTURE` | Must |
| EF7 | Le formateur consulte le tableau de sa promotion | Quand je choisis une promotion, alors je vois une ligne par étudiant avec présences, exercices déposés, moyenne des notes reçues (vide s'il n'a aucune note) et relectures en attente, toutes lues dans `GET /api/tableau` sans recalcul par le front. Promotion inexistante : `404 PROMOTION_INCONNUE` | Must |
| EF8 | Un étudiant qui se trompe 5 fois de suite de code est bloqué 2 minutes | Quand je saisis 5 codes inconnus d'affilée, alors toute tentative suivante, même avec le bon code, reçoit `429 TROP_DE_TENTATIVES` pendant 2 min ; après ce délai, un code valide est accepté | Should |
| EF9 | Le formateur ajoute une présence à la main | Quand j'ajoute un étudiant de la promotion à une session non clôturée, alors je reçois `201` avec `source = FORMATEUR` et il apparaît « ajouté par le formateur » dans la liste des présents, y compris après l'expiration du code. S'il est déjà présent : `409 DEJA_PRESENT` | Should |
| EF10 | Le formateur clôture une session | Quand je clôture une session, alors plus aucune présence, aucun dépôt ni remplacement de lien n'est accepté (`410 SESSION_CLOTUREE` pour la présence, `409 SESSION_CLOTUREE` sinon), mais les relectures déjà assignées peuvent encore être rendues | Should |
| EF11 | L'étudiant remplace le lien de son exercice tant que la relecture n'a pas commencé | Quand mon relecteur n'a pas encore cliqué « Commencer », alors `PUT /api/exercices/{id}` remplace le lien (`200`). Après : `409 RELECTURE_COMMENCEE` | Should |
| EF12 | L'étudiant consulte la note et le commentaire reçus, sans le nom du relecteur | Quand ma relecture est rendue, alors je vois la note et le commentaire sur mon écran ; aucune réponse de l'API destinée à l'auteur ne contient l'identité du relecteur | Should |
| EF13 | Le formateur voit, pour chaque session, les présents et les exercices avec leur statut | Quand j'ouvre une session, alors je vois la liste des présents avec leur source et la liste des exercices ; un exercice dont la relecture n'a pas été rendue est marqué « en attente » de façon visible (Q11) | Should |
| EF14 | Le tableau détaille la présence de chaque étudiant session par session | Quand j'affiche le détail, alors je vois une grille étudiants × sessions indiquant présent (étudiant ou formateur) ou absent | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'écran étudiant (présence et dépôt) est utilisable sur un téléphone | Outils de développement du navigateur en 360 × 640 : aucun défilement horizontal, champ du code et boutons lisibles et cliquables au doigt (≥ 44 px de haut) |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants et 30 sessions | Indicateurs calculés par des requêtes agrégées (une par indicateur, pas de requête par étudiant) ; mesure `curl -w "%{time_total}"` sur `GET /api/tableau` |
| ENF3 | Volumétrie cible : jusqu'à 5 promotions de 60 étudiants, environ 3 sessions par semaine, pic de 60 marquages de présence dans les 2 minutes qui suivent l'affichage du code | Contraintes d'unicité en base (`presence`, `exercice`, `session_cours.code`) : aucun doublon possible même en cas de requêtes simultanées ; la violation est traduite en `409` |
| ENF4 | Toute erreur de l'API respecte le format `{ code, message }` : jamais de stack trace, de corps vide ni de page d'erreur par défaut de Spring | Tests d'intégration sur chaque code d'erreur, y compris route inconnue, JSON malformé et méthode non autorisée |
| ENF5 | Un tiers démarre l'application depuis le seul `README`, avec des données de démonstration | Clone dans un dossier vide, puis `docker compose up --build` : le front s'ouvre et le tableau de la promotion de démonstration n'est pas vide |
| ENF6 | Les tests tournent sur un poste vierge, sans base de données locale | `./mvnw test` sur H2 en mémoire, exécuté par la CI GitHub Actions à chaque pull request |
| ENF7 | Les dates échangées sont au format ISO-8601 avec fuseau (UTC côté serveur) et affichées à l'heure locale côté front | Contrat `format: date-time` ; test d'intégration sur `ouvertureAt` et `expirationAt` |
| ENF8 | L'interface et les messages d'erreur sont en français ; le front affiche le `message` renvoyé par l'API | Revue des trois écrans ; provoquer chaque erreur métier et lire le message affiché |
| ENF9 | Le relecteur reste anonyme pour l'auteur (RG19) | Test d'intégration : la réponse de `GET /api/etudiants/{id}/exercices` ne contient ni l'identifiant ni le nom du relecteur |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session (`expirationAt = ouvertureAt + 15 min`). Au-delà, `POST /api/presences` répond `410 CODE_EXPIRE` | Q2 |
| RG2 | Un étudiant a au plus une présence par session, quelle que soit sa source. Une seconde tentative répond `409 DEJA_PRESENT` | Besoin §2, Q14 |
| RG3 | Le code est composé de 6 caractères tirés de `A–Z` et `2–9`, sans les caractères ambigus `0 O 1 I`. Il est unique parmi toutes les sessions, passées comprises, et n'est jamais réutilisé | Z2 |
| RG4 | Un code qui ne correspond à aucune session de la promotion de l'étudiant répond `400 CODE_INCONNU`, y compris le code valide d'une autre promotion | Z2 |
| RG5 | Après 5 réponses `CODE_INCONNU` consécutives, l'étudiant est bloqué 2 minutes : toute tentative répond `429 TROP_DE_TENTATIVES`. Le compteur revient à zéro après une présence réussie ou à la fin du blocage | Q4, Z8 |
| RG6 | Une présence marquée par l'étudiant a la source `ETUDIANT` ; une présence ajoutée par le formateur a la source `FORMATEUR` et s'affiche « ajouté par le formateur » | Q14 |
| RG7 | L'étudiant ne marque sa présence que tant que le code est valide et que la session n'est pas clôturée ; le formateur peut ajouter une présence à la main jusqu'à la clôture, même après l'expiration du code | Q2, Q3, Q14, C5 |
| RG8 | Un étudiant dépose au plus un exercice par session. Un second dépôt répond `409 EXERCICE_DEJA_DEPOSE` ; pour changer de lien, il le remplace (RG22) | Contrat, Q13 |
| RG9 | Le lien est une URL absolue en `http://` ou `https://` d'au plus 500 caractères ; sinon `400 LIEN_INVALIDE` | Z16 |
| RG10 | Le dépôt et le remplacement d'un lien sont acceptés tant que la session n'est pas clôturée, même après l'expiration du code ; sinon `409 SESSION_CLOTUREE` | Q12 |
| RG11 | Chaque exercice a exactement un relecteur | Q6 |
| RG12 | Le relecteur est tiré au hasard par le système parmi les étudiants présents à la session de l'exercice, auteur exclu. Le tirage se fait parmi les éligibles qui ont le moins de relectures assignées dans cette session | Q5, Q7, Z6 |
| RG13 | Si aucun étudiant éligible n'est présent au moment du dépôt, l'exercice reste `DEPOSE`, sans relecteur. Il est assigné dès qu'un étudiant éligible devient présent, par le code ou par le formateur | Z3 |
| RG14 | Un étudiant ne relit jamais son propre exercice : `403 AUTO_RELECTURE` | Q5 |
| RG15 | Seul le relecteur assigné peut commencer ou rendre une relecture : `403 RELECTEUR_NON_ASSIGNE` | Q6, Q7, Z1 |
| RG16 | La note est un entier de 0 à 20 (`400 NOTE_INVALIDE`). Le commentaire est obligatoire, de 1 à 2000 caractères (`400 COMMENTAIRE_INVALIDE`) | Q9, Z15 |
| RG17 | Une relecture rendue est définitive : un second envoi répond `409 RELECTURE_DEJA_RENDUE` | Q15, C1 |
| RG18 | La moyenne d'un étudiant est la moyenne arithmétique des notes reçues sur ses exercices relus, arrondie à 2 décimales. Elle vaut `null` s'il n'a reçu aucune note. Elle est calculée uniquement par l'API | Q16, Z11, F3 |
| RG19 | L'étudiant relu voit sa note et son commentaire, jamais l'identité de son relecteur | Q8 |
| RG20 | La clôture d'une session est définitive. Elle bloque les présences, les dépôts et les remplacements de lien, pas les relectures déjà assignées | Q10, Q12, Z4, Z12 |
| RG21 | Dans le tableau : `presences` = nombre de sessions de la promotion où l'étudiant est présent, toutes sources confondues ; `exercicesDeposes` = nombre de ses exercices déposés ; `relecturesEnAttente` = nombre de relectures qui lui sont assignées et non rendues | Q11, Q16 |
| RG22 | Le lien d'un exercice peut être remplacé par son auteur tant que le relecteur n'a pas commencé la relecture ; ensuite `409 RELECTURE_COMMENCEE` | Q13, Z7 |
| RG23 | Un étudiant ne dépose un exercice que pour une session de sa propre promotion ; sinon `400 ETUDIANT_HORS_PROMOTION` | Z5 |
