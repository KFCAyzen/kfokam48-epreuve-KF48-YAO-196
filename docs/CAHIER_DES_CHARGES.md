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

## 7. Zones d'ombre, hypothèses et contradictions

### 7.1 Contradictions relevées

La contradiction franche est C1 : deux réponses du client s'excluent. C2 à C5 sont des tensions entre la demande, les réponses et le contrat imposé, qu'il faut aussi trancher.

| Réf | Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|---|
| **C1** | **Q10** « un relecteur peut corriger sa note tant que la session n'est pas clôturée » contre **Q15** « une fois validée, c'est fini, il ne peut plus y revenir » | **Q15 : une relecture rendue est définitive (RG17). Q10 est écartée** | 1) Le **contrat imposé** tranche déjà : `POST /api/relectures/{id}` prévoit `409 RELECTURE_DEJA_RENDUE`, donc un second envoi est un conflit et non une correction. Retenir Q10 obligerait à contourner une opération imposée. 2) Q15 est la réponse la plus explicite et le client la justifie (« plus honnête pour tout le monde »). 3) Une note corrigeable après coup affaiblit la relecture entre pairs, qui est le cœur du besoin. Conséquence : la clôture ne sert plus à figer les notes (RG20) |
| C2 | **Q16** « sa présence *à chaque session* » contre le contrat imposé, où `presences` est un **entier** | Le contrat est respecté à la lettre : `GET /api/tableau` renvoie un nombre. Le détail session par session est une opération distincte, `GET /api/tableau/presences` (EF14, Could) | Le contrat est non négociable (B2). Le détail répond au besoin de Q16 sans modifier l'opération imposée |
| C3 | La demande initiale (« présence et moyenne des notes par étudiant ») contre **Q16** (quatre indicateurs) | Q16 | Elle est plus précise, plus récente, et le contrat imposé reprend ses quatre indicateurs |
| C4 | **Q13** « on peut remplacer le lien » contre le contrat : `POST /api/exercices` répond `409 EXERCICE_DEJA_DEPOSE` au second dépôt | Pas de vraie contradiction : le dépôt (`POST`) n'a lieu qu'une fois, le remplacement est une opération distincte `PUT /api/exercices/{id}` (EF11, RG22) | Le code `409` imposé interdit le double dépôt, pas la modification |
| C5 | **Q3** « pas de présence après la fin de la session » contre **Q14** « le formateur ajoute une présence à la main » | Q3 s'applique à l'étudiant ; Q14 est l'exception du formateur, possible jusqu'à la clôture (RG7) | Le cas décrit en Q14 (un souci de téléphone) se règle justement après la fenêtre de 15 minutes |

### 7.2 Zones d'ombre et hypothèses

**Z1 est le trou que personne n'a vu** : le contrat imposé prévoit `403 AUTO_RELECTURE`, mais aucune opération ne dit **qui** appelle.

| Réf | Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|---|
| **Z1** | `POST /api/relectures/{id}` ne reçoit que `{ note, commentaire }` : l'API ne sait pas qui rend la relecture, donc elle ne peut pas détecter une auto-relecture | Aucune question ne le couvre. Q1 : pas de mot de passe, l'étudiant choisit son nom | L'appelant se désigne par l'en-tête **`X-Etudiant-Id`**, identité déclarative cohérente avec Q1. Le front l'envoie toujours. L'en-tête est **facultatif** : sans lui, la relecture est attribuée au relecteur assigné, pour qu'une requête conforme au seul corps imposé fonctionne | RG14, RG15 ; l'en-tête est ajouté au contrat |
| Z2 | `POST /api/presences` reçoit un code, pas de session : comment retrouver la session, et que répondre si un code est réutilisé ou vient d'une autre promotion ? | Hypothèse | Le code est **unique pour toujours** (contrainte d'unicité en base) : un code expiré désigne encore sa session, on peut donc répondre `410` et non `400`. Le code d'une autre promotion est traité comme inconnu | RG3, RG4 |
| Z3 | Q7 : relecteur tiré parmi les présents. Et si l'auteur est seul présent, ou si personne n'est présent au moment du dépôt ? | Hypothèse | L'exercice reste `DEPOSE` et reçoit un relecteur dès qu'un étudiant éligible devient présent | RG13, statut `DEPOSE` en D4 |
| Z4 | « La fin de la session » (Q3, Q10, Q12) n'est jamais définie : aucune heure de fin n'est demandée | Hypothèse | Pas d'heure de fin. Pour l'étudiant, la fenêtre de présence est celle du code (15 min). La **clôture** est une action explicite du formateur (EF10) | Colonne `cloturee_at`, opération `POST /api/sessions/{id}/cloture` |
| Z5 | Un étudiant absent peut-il déposer un exercice ? Q12 autorise le dépôt après la séance, Q7 réserve la relecture aux présents | Hypothèse | Oui : l'exercice est un travail, pas une présence. En revanche il ne sera relecteur d'aucun exercice de cette session. Il ne peut déposer que pour une session de sa promotion | RG23 |
| Z6 | Le hasard pur peut confier 5 relectures au même étudiant et aucune à un autre | Q7 dit « au hasard », rien sur l'équité | Tirage au hasard **parmi les éligibles les moins chargés** dans la session : c'est toujours le système qui choisit, au hasard | RG12 |
| Z7 | Q13 : « tant que personne n'a commencé à le relire ». Le début d'une relecture n'est pas observable | Hypothèse | Le relecteur clique « Commencer la relecture », ce qui lui révèle le lien et horodate `commencee_at`. Le lien n'est plus remplaçable ensuite | RG22, statut `EN_COURS_DE_RELECTURE` |
| Z8 | Q4 : bloquer qui (l'étudiant ou le poste) ? Quelles erreurs comptent ? Avec quel code HTTP, absent du contrat ? | Q4 : 5 erreurs, 2 minutes | Par étudiant, seule identité connue (Q1). Seules les réponses `CODE_INCONNU` comptent, pas un code expiré ni une présence en double. Code **`429 TROP_DE_TENTATIVES`**, ajouté au contrat | RG5, table `blocage_code` |
| Z9 | Qui crée les promotions et les étudiants ? | Aucune question | Hors périmètre : des données de démonstration les créent au démarrage | §3 Exclu |
| Z10 | Le formateur a-t-il un compte ? | Q1 : pas de mot de passe | Aucun compte formateur : l'écran formateur est accessible sans identification. Risque accepté et documenté | §2 |
| Z11 | Moyenne : arrondi ? valeur pour un étudiant sans note ? | Contrat : `moyenne` nullable | Arrondi à 2 décimales ; `null` s'il n'a reçu aucune note ; le front affiche « — » | RG18 |
| Z12 | Une relecture peut-elle être rendue après la clôture ? | Q10 lie les corrections à la clôture, mais Q10 est écartée (C1) | Oui : sinon un exercice en attente le resterait pour toujours, ce que Q11 cherche justement à éviter | RG20 |
| Z13 | `POST /api/sessions` avec une promotion inexistante : quel code ? Le contrat ne prévoit que `400` | Hypothèse | `400 PROMOTION_INCONNUE` : on reste dans les codes prévus pour l'opération imposée | Contrat |
| Z14 | Quel identifiant dans `POST /api/relectures/{id}` ? Aucune opération imposée ne renvoie un identifiant de relecture | Q6 : un seul relecteur par exercice | Relation 1–1 : la relecture **partage l'identifiant de son exercice** (clé primaire `relecture.exercice_id`). L'`id` renvoyé par `POST /api/exercices` est donc utilisable tel quel | D2 |
| Z15 | Le commentaire peut-il être vide ? | Le contrat le déclare obligatoire | Obligatoire, de 1 à 2000 caractères | RG16 |
| Z16 | Qu'est-ce qu'un lien valide ? | Contrat : `format: uri` | URL absolue en `http(s)` de 500 caractères au plus ; son contenu n'est pas vérifié | RG9 |
| Z17 | Fuseau horaire des dates | Hypothèse | Instants stockés et échangés en UTC (ISO-8601), affichés à l'heure locale | ENF7 |

**Questions sans effet sur le produit :** Q10, écartée par C1. Q1 supprime l'authentification du périmètre plus qu'elle n'ajoute une fonctionnalité.

## 8. Contraintes techniques

**Imposées par le sujet :**

| Réf | Contrainte | Comment je la respecte |
|---|---|---|
| B1 | Java 17+, Maven, wrapper `mvnw` commité | Java 21, Spring Boot 3, `mvnw` et `.mvn/wrapper` versionnés |
| B2 | Contrat `api/contrat.yaml` respecté à la lettre | Chemins, verbes, codes et format d'erreur des 5 opérations imposées inchangés ; les ajouts sont marqués dans le fichier ; tests d'intégration sur les codes |
| B3 | Couches contrôleur / service / repository, aucune entité JPA en JSON | Paquets `controller`, `service`, `repository`, `entity`, `dto` ; les contrôleurs ne manipulent que des DTO (records) |
| B4 | Validation des entrées, erreurs centralisées, jamais de stack trace | Bean Validation sur les DTO, un `@RestControllerAdvice` unique, et un contrôleur `/error` qui renvoie aussi `{ code, message }` |
| B5 | Schéma versionné, `ddl-auto=update` interdit | Flyway (`db/migration/V1__…`), `ddl-auto=validate` |
| B6 | Un test unitaire sur une règle réelle et un test d'intégration sur un endpoint, sans base locale | Tests unitaires sur RG1, RG12, RG14 et RG5 ; tests d'intégration MockMvc sur H2 en mémoire |
| F1 | Framework déclaré et justifié dans le README, build qui passe | React + Vite + TypeScript ; `npm run build` exécuté par la CI |
| F2 | Trois écrans : formateur, étudiant, relecteur | Routes `/formateur`, `/etudiant`, `/relecteur` |
| F3 | Appels API dans une couche dédiée, états de chargement et d'erreur, aucune règle métier dupliquée | Dossier `src/api/` seul à appeler `fetch` ; moyenne, statuts et contrôles lus depuis l'API |

**Que je m'impose :**
- **Base :** PostgreSQL 16 dans `docker compose` ; H2 en mémoire, en mode PostgreSQL, pour les tests et le lancement sans Docker. Les migrations sont écrites en SQL compatible avec les deux.
- **Heure :** une `Clock` injectée, pour tester l'expiration du code sans attendre 15 minutes.
- **Hasard :** le tirage du relecteur est isolé dans une classe pure, testable avec une graine fixe.
- **Démarrage :** `docker compose up --build` construit le backend et le front dans des conteneurs ; rien d'autre à installer que Docker.
- **CI :** GitHub Actions lance `./mvnw verify` et `npm run build` à chaque pull request, pour que `main` reste sain.
- **Git :** une branche par ticket, une pull request par branche, fusion sans squash pour garder les commits atomiques, issues fermées par `Closes #n`.

## 9. Livrables

- Dépôt public `kfokam48-epreuve-KF48-YAO-196` :
  - `docs/CAHIER_DES_CHARGES.md` (ce document, tenu à jour après l'étape 3) ;
  - `docs/JOURNAL.md`, une entrée par étape ;
  - `docs/diagrammes/` : D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 états d'un exercice (bonus), en Mermaid ;
  - `api/contrat.yaml` : les 5 opérations imposées et les opérations ajoutées ;
  - `backend/` : API Spring Boot, migrations Flyway, tests ;
  - `frontend/` : application React, trois écrans ;
  - `docker-compose.yml`, `README.md` (installation testée depuis un clone vierge), `CHANGELOG.md` ;
  - backlog en issues priorisées, pull requests liées, trois commits `[JALON]`.
- Dépôt public `kfokam48-gitlab-KF48-YAO-196` : l'épreuve Git de l'étape 5.
- `SOUMISSION.md`, téléversé sur la plateforme avant 18h00.

## 10. Démarche prévue

| Étape | Créneau visé | Ce que je vise | Fin de l'étape |
|---|---|---|---|
| 1. Analyser | 10h15 – 11h45 | Ce cahier des charges, D1 à D4, backlog en issues, contrat complété et figé | Journal, puis `[JALON] analyse` poussé |
| 2. Première version | 11h45 – 14h15 | Les stories **Must** uniquement : EF1 à EF7, plus le socle technique (démarrage, format d'erreur) | Journal, puis `[JALON] v0.1` poussé |
| 3. Enveloppe | 14h15 – 15h45 | Issues ouvertes **avant** de coder, bug reproduit par un test, migration versionnée, contrat mis à jour, re-priorisation écrite, correctif et évolution sur deux branches séparées, cahier et diagrammes mis à jour | Journal |
| 4. Version finale | 15h45 – 16h45 | Les Should restants, CHANGELOG, README testé depuis un clone vierge, backlog restant trié | Journal, puis `[JALON] v1.0` poussé |
| 5. Épreuve Git | 16h45 – 17h15 | Les cinq situations de `git-lab.bundle`, dans un second dépôt | Journal |
| 6. Soumettre | 17h15 – 17h30 | Hash relevés, liens vérifiés en navigation privée, `SOUMISSION.md` téléversé | 30 min de marge avant 18h00 |

**Si je prends du retard :** je coupe d'abord le Could (EF14), puis les Should dans l'ordre EF13, EF12, EF11. Je ne coupe jamais les tests B6, le journal, le README ni la mise à jour de l'analyse après l'étape 3. Tout ticket coupé reste ouvert dans le backlog avec sa priorité.

**Flux Git par ticket :** issue → branche `feat/<n>-<sujet>` (ou `fix/<n>-<sujet>`) → commits atomiques au format `type(portée): message` qui citent les `RGx` concernées → pull request « Closes #n » → CI verte → fusion dans `main`.

**Definition of Done — un ticket est terminé quand :**
- chaque critère d'acceptation de l'issue est vérifié, à la main ou par un test ;
- les règles de gestion citées par l'issue sont couvertes par au moins un test ;
- les opérations touchées respectent `api/contrat.yaml` : chemins, codes et format d'erreur ;
- le backend compile, `./mvnw verify` passe et `npm run build` passe (CI verte) ;
- aucun fichier généré et aucun secret n'est commité ;
- la pull request est liée à l'issue et fusionnée dans `main`, et l'issue est fermée ;
- le cahier des charges et les diagrammes sont à jour si le ticket en change le contenu.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25/09/2026, étape 1 | Version initiale |
