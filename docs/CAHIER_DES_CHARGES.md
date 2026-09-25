# Cahier des charges — Présence & relecture entre pairs KFOKAM48

**Auteur :** KEPSEU Franck Celestin · KF48-YAO-196
**Version :** 2.1 · **Date :** 25/09/2026
**Frontend choisi :** React, parce que trois écrans de formulaires et de tableaux n'ont besoin ni de rendu serveur (Next.js) ni d'un framework complet (Angular) : React avec Vite donne le code le plus court et un build statique simple à servir dans `docker compose`.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 suit aujourd'hui la présence en cours et les exercices des étudiants à la main. Deux problèmes en découlent :

- **la présence n'est pas fiable** : un appel papier se contourne, et rien n'empêche qu'un étudiant absent soit déclaré présent par un camarade ;
- **les exercices ne sont pas relus** : le formateur ne peut pas corriger seul chaque exercice de chaque session.

L'application répond aux deux avec un seul outil. Le formateur ouvre une session de cours et affiche un **code de présence à durée courte**, que seuls les étudiants dans la salle peuvent saisir à temps. Chaque étudiant dépose le **lien** de son exercice. Le système confie chaque exercice à **deux pairs présents**, tirés au hasard, qui le notent et le commentent ; la note retenue est la moyenne des deux. Le formateur suit l'ensemble dans un **tableau par étudiant** : présences, exercices déposés, moyenne des notes reçues, relectures encore dues.

L'objectif : en fin de séance, le formateur sait sans rien ressaisir qui était là, qui a rendu son travail, quelle note il a reçue, provisoire ou définitive, et qui n'a pas fait sa relecture.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| Formateur | Ouvrir une session et obtenir son code (EF2) · ajouter une présence à la main (EF9) · clôturer une session (EF10) · voir les présents et les exercices d'une session (EF13) · consulter le tableau de sa promotion (EF7, EF14) | Relire ou noter un exercice · modifier une note · supprimer une présence · choisir le relecteur d'un exercice |
| Étudiant | Se désigner dans la liste de sa promotion (EF3) · marquer sa présence avec le code (EF1) · déposer le lien de son exercice (EF4) et le remplacer (EF11) · voir la note et le commentaire reçus (EF12) | Marquer sa présence après l'expiration du code, ou deux fois · déposer deux exercices pour la même session · choisir son relecteur · connaître le nom de son relecteur · relire son propre exercice |
| Relecteur | Voir les exercices qui lui sont assignés · commencer une relecture, ce qui lui révèle le lien · rendre une note et un commentaire (EF6) | Relire son propre exercice · relire un exercice qui ne lui est pas assigné · modifier une relecture déjà rendue |

**Le relecteur n'est pas un acteur distinct : c'est un étudiant, dans le rôle que lui donne une relecture qui lui a été assignée.** Un même étudiant est auteur de son exercice et relecteur de celui d'un pair pendant la même session. Conséquence sur le modèle de données : il n'y a pas de table `relecteur` ; la table `relecture` porte une clé étrangère `relecteur_id` vers `etudiant` (voir D2).

Le **système** intervient comme acteur secondaire : il génère le code, tire le relecteur au hasard (EF5), bloque un étudiant après 5 erreurs de code (EF8) et calcule la moyenne. Le formateur n'a pas de compte (Q1) : il n'est pas représenté en base et l'écran formateur est accessible sans identification (voir section 7).

## 3. Périmètre

**Inclus dans cette version :**
- ouverture d'une session de cours pour une promotion, avec un code de présence valable 15 minutes ;
- marquage de la présence par l'étudiant avec le code ;
- dépôt du lien d'un exercice par session ;
- attribution automatique et aléatoire de **deux relecteurs différents** parmi les étudiants présents ;
- relecture : note entière sur 20 et commentaire, définitive une fois rendue ; note retenue = moyenne des deux, provisoire tant qu'une seule est rendue ;
- consultation par l'étudiant de sa note retenue et des commentaires, sans le nom des relecteurs ;
- tableau récapitulatif par étudiant pour une promotion ;
- identification sans mot de passe, par choix dans une liste ;
- données de démonstration chargées au démarrage, lancement par `docker compose up`.

**Explicitement exclu :**
- authentification, mots de passe, rôles et comptes (Q1) : l'identité est déclarative ;
- création, modification et suppression des promotions, des étudiants et des formateurs : elles viennent des données de démonstration ;
- correction d'une note déjà rendue (Q15 retenue contre Q10, voir section 7) ;
- réattribution manuelle d'un relecteur par le formateur, ou désistement d'un relecteur ;
- vérification que le lien déposé est accessible ou pointe vers un vrai dépôt : seule la forme de l'URL est contrôlée ;
- notifications (e-mail, SMS, push) ;
- export du tableau (CSV, PDF) et statistiques au-delà des quatre indicateurs de Q16 ;
- historique ou audit des modifications ;
- mode hors ligne, application mobile native, interface multilingue ;
- réouverture d'une session clôturée ;
- **sortis du périmètre de la v1.0 à l'étape 3**, pour absorber la double relecture (section 10) : blocage après 5 codes faux (EF8, #10), ajout manuel d'une présence (EF9, #11), clôture d'une session (EF10, #12), remplacement du lien (EF11, #13), détail d'une session (EF13, #15), grille de présence (EF14, #16). Ces stories restent dans le backlog avec leur priorité.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | L'étudiant marque sa présence à l'aide d'un code | Quand je saisis un code valide et non expiré, alors ma présence apparaît dans le tableau du formateur (`presences` + 1) et `POST /api/presences` a répondu `201` avec `source = ETUDIANT`. Quand le code n'existe pas, alors `400 CODE_INCONNU` ; quand il a expiré, alors `410 CODE_EXPIRE` ; quand je suis déjà présent, alors `409 DEJA_PRESENT` | Must |
| EF2 | Le formateur ouvre une session pour une promotion et obtient un code de présence | Quand je saisis un titre, choisis une promotion et valide, alors un code de 6 caractères s'affiche avec son heure d'expiration, égale à l'heure d'ouverture + 15 min (`201 { id, code, ouvertureAt, expirationAt }`). Quand le titre manque, alors `400 CHAMP_MANQUANT` | Must |
| EF3 | L'utilisateur se désigne en choisissant sa promotion puis son nom dans une liste, sans mot de passe | Quand j'ouvre l'écran étudiant ou relecteur, alors je choisis ma promotion puis mon nom dans des listes fournies par l'API, et mon choix est conservé jusqu'à ce que je clique « Changer d'identité » | Must |
| EF4 | L'étudiant dépose le lien de son exercice pour une session de sa promotion | Quand je choisis une session non clôturée et saisis une URL `http(s)://…`, alors je reçois `201 { id, statut }` et `exercicesDeposes` augmente de 1 dans le tableau. Quand je dépose une seconde fois pour la même session, alors `409 EXERCICE_DEJA_DEPOSE` ; quand l'URL est invalide, alors `400 LIEN_INVALIDE` | Must |
| EF5 | Le système assigne à chaque exercice **deux relecteurs différents**, tirés au hasard parmi les étudiants présents à la session, jamais l'auteur | Quand un exercice est déposé et qu'au moins deux autres étudiants sont présents, alors il reçoit deux relecteurs différents et passe `EN_ATTENTE_RELECTURE`. Quand un seul éligible est présent, alors il en reçoit un, et le second dès qu'un autre éligible marque sa présence. Quand aucun n'est présent, alors il reste `DEPOSE` | Must |
| EF6 | Le relecteur commence la relecture d'un exercice qui lui est assigné, puis rend une note et un commentaire | Quand je clique « Commencer », alors le lien de l'exercice s'affiche. Quand j'envoie la note 14 et un commentaire, alors je reçois `200` ; l'exercice passe `RELU` quand ses deux relectures sont rendues. Quand la note vaut 12.5 ou 21, alors `400 NOTE_INVALIDE` ; quand j'envoie une seconde fois, alors `409 RELECTURE_DEJA_RENDUE` ; quand l'auteur tente de relire son propre exercice, alors `403 AUTO_RELECTURE` | Must |
| EF7 | Le formateur consulte le tableau de sa promotion | Quand je choisis une promotion, alors je vois une ligne par étudiant avec ses présences, ses exercices déposés, la moyenne de ses notes retenues (« — » s'il n'en a aucune, signalée « provisoire » si l'une d'elles l'est) et ses relectures en attente, toutes lues dans `GET /api/tableau` sans recalcul par le front. Quand la promotion n'existe pas, alors `404 PROMOTION_INCONNUE` | Must |
| EF8 | Un étudiant qui se trompe 5 fois de suite de code est bloqué 2 minutes | Quand je saisis 5 codes inconnus d'affilée, alors toute tentative suivante, même avec le bon code, reçoit `429 TROP_DE_TENTATIVES` pendant 2 min ; quand ce délai est passé, alors un code valide est accepté | Should — sorti de la v1.0 (étape 3) |
| EF9 | Le formateur ajoute une présence à la main | Quand j'ajoute un étudiant de la promotion à une session non clôturée, y compris après l'expiration du code, alors je reçois `201` avec `source = FORMATEUR` et l'étudiant apparaît « ajouté par le formateur » dans la liste des présents. Quand il est déjà présent, alors `409 DEJA_PRESENT` | Should — sorti de la v1.0 (étape 3) |
| EF10 | Le formateur clôture une session | Quand je clôture une session, alors plus aucune présence (`410 SESSION_CLOTUREE`), aucun dépôt ni remplacement de lien (`409 SESSION_CLOTUREE`) n'est accepté, mais les relectures déjà assignées peuvent encore être rendues | Should — sorti de la v1.0 (étape 3) |
| EF11 | L'étudiant remplace le lien de son exercice tant que la relecture n'a pas commencé | Quand mon relecteur n'a pas encore cliqué « Commencer », alors `PUT /api/exercices/{id}` remplace le lien (`200`). Quand il a commencé, alors `409 RELECTURE_COMMENCEE` | Should — sorti de la v1.0 (étape 3) |
| EF12 | L'étudiant consulte la note retenue et les commentaires reçus, sans le nom des relecteurs | Quand une seule de mes deux relectures est rendue, alors je vois sa note marquée « provisoire » ; quand les deux le sont, alors je vois leur moyenne et les deux commentaires. Aucune réponse de l'API destinée à l'auteur ne contient l'identité d'un relecteur | Must (était Should, promue à l'étape 3) |
| EF13 | Le formateur voit, pour chaque session, les présents et les exercices avec leur statut | Quand j'ouvre une session, alors je vois les présents avec leur source et les exercices ; quand une relecture n'a pas été rendue, alors l'exercice est marqué « en attente » de façon visible (Q11) | Should — sorti de la v1.0 (étape 3) |
| EF14 | Le tableau détaille la présence de chaque étudiant session par session | Quand j'affiche le détail d'une promotion, alors je vois une grille étudiants × sessions indiquant présent (par l'étudiant ou par le formateur) ou absent | Could — sorti de la v1.0 (étape 3) |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'interface de marquage de présence est utilisable sur un téléphone | Outils de développement du navigateur en 360 × 640 : aucun défilement horizontal, champ du code et boutons d'au moins 44 px de haut |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Indicateurs calculés par des requêtes agrégées (une par indicateur, aucune requête par étudiant) ; mesure `curl -w "%{time_total}"` sur `GET /api/tableau` |
| ENF3 | Volumétrie cible : jusqu'à 5 promotions de 60 étudiants, environ 3 sessions par semaine, pic de 60 marquages de présence dans les 2 minutes qui suivent l'affichage du code | Contraintes d'unicité en base sur `presence`, `exercice` et `session_cours.code` : aucun doublon possible même avec des requêtes simultanées ; la violation est traduite en `409` |
| ENF4 | Toute erreur de l'API respecte le format `{ code, message }` : jamais de stack trace, de corps vide ni de page d'erreur par défaut de Spring | Tests d'intégration sur chaque code d'erreur, y compris route inconnue, JSON malformé et méthode non autorisée |
| ENF5 | Un tiers démarre l'application depuis le seul `README`, avec des données de démonstration | Clone dans un dossier vide puis `docker compose up --build` : le front s'ouvre et le tableau de la promotion de démonstration n'est pas vide |
| ENF6 | Les tests tournent sur un poste vierge, sans base de données installée localement | `./mvnw verify` démarre un PostgreSQL 16 jetable (Testcontainers) : Docker suffit ; exécuté par la CI GitHub Actions à chaque pull request |
| ENF7 | Les dates échangées sont au format ISO-8601 en UTC et affichées à l'heure locale | Contrat `format: date-time` ; test d'intégration sur `ouvertureAt` et `expirationAt` |
| ENF8 | L'interface et les messages d'erreur sont en français ; le front affiche le `message` renvoyé par l'API | Provoquer chaque erreur métier depuis les trois écrans et lire le message affiché |
| ENF9 | Le relecteur reste anonyme pour l'auteur (RG20) | Test d'intégration : la réponse de `GET /api/etudiants/{id}/exercices` ne contient ni l'identifiant ni le nom du relecteur |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session (`expirationAt = ouvertureAt + 15 min`). Au-delà : `410 CODE_EXPIRE` | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice : `403 AUTO_RELECTURE` | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 : sinon `400 NOTE_INVALIDE` | Q9 |
| RG4 | Un étudiant a au plus une présence par session, quelle que soit sa source : sinon `409 DEJA_PRESENT` | Demande §2, Q14 |
| RG5 | Le code est composé de 6 caractères tirés de `A–Z` et `2–9`, sans les caractères ambigus `0 O 1 I`. Il est unique parmi toutes les sessions, passées comprises, et n'est jamais réutilisé | Hypothèse (section 7) |
| RG6 | Un code qui ne correspond à aucune session de la promotion de l'étudiant répond `400 CODE_INCONNU`, y compris le code valide d'une autre promotion | Hypothèse (section 7) |
| RG7 | Après 5 réponses `CODE_INCONNU` consécutives, l'étudiant est bloqué 2 minutes : toute tentative répond `429 TROP_DE_TENTATIVES`. Le compteur revient à zéro après une présence réussie ou à la fin du blocage | Q4 |
| RG8 | Une présence marquée par l'étudiant a la source `ETUDIANT` ; une présence ajoutée par le formateur a la source `FORMATEUR` et s'affiche « ajouté par le formateur » | Q14 |
| RG9 | L'étudiant ne marque sa présence que tant que le code est valide et que la session n'est pas clôturée ; le formateur peut ajouter une présence à la main jusqu'à la clôture, même après l'expiration du code | Q2, Q3, Q14 |
| RG10 | Un étudiant dépose au plus un exercice par session : sinon `409 EXERCICE_DEJA_DEPOSE`. Pour changer de lien, il le remplace (RG23) | Contrat, Q13 |
| RG11 | Le lien est une URL absolue en `http://` ou `https://` d'au plus 500 caractères : sinon `400 LIEN_INVALIDE` | Contrat (`format: uri`) |
| RG12 | Le dépôt et le remplacement d'un lien sont acceptés tant que la session n'est pas clôturée, même après l'expiration du code : sinon `409 SESSION_CLOTUREE` | Q12 |
| RG13 | Chaque exercice a exactement **deux relecteurs différents** | Enveloppe de l'étape 3 (remplace Q6) |
| RG14 | Les deux relecteurs sont tirés au hasard par le système parmi les étudiants présents à la session de l'exercice, auteur exclu, et parmi ceux-là, parmi les moins chargés en relectures dans cette session ; un même étudiant ne relit pas deux fois le même exercice | Q5, Q7, enveloppe |
| RG15 | S'il y a moins de deux étudiants éligibles au moment du dépôt, l'exercice reçoit ceux qui sont disponibles ; chaque place manquante est pourvue dès qu'un étudiant éligible devient présent. Sans aucun relecteur, l'exercice reste `DEPOSE` | Hypothèse (section 7), enveloppe |
| RG16 | Seul le relecteur assigné à une relecture peut la commencer ou la rendre : sinon `403 RELECTEUR_NON_ASSIGNE` | Q6, Q7 |
| RG17 | Le commentaire d'une relecture est obligatoire, de 1 à 2000 caractères : sinon `400 COMMENTAIRE_INVALIDE` | Contrat (`commentaire` requis) |
| RG18 | Chacune des deux relectures, une fois rendue, est définitive : un second envoi répond `409 RELECTURE_DEJA_RENDUE` | Q15 (retenue contre Q10) |
| RG19 | La moyenne d'un étudiant est la moyenne arithmétique des notes retenues de ses exercices (RG25), arrondie à 2 décimales ; elle vaut `null` s'il n'en a aucune ; elle est signalée provisoire si l'une des notes retenues l'est ; seule l'API la calcule | Q16, enveloppe |
| RG20 | L'étudiant relu voit la note retenue et les commentaires, jamais l'identité de ses relecteurs | Q8 |
| RG21 | La clôture d'une session est définitive ; elle bloque les présences, les dépôts et les remplacements de lien, pas les relectures déjà assignées | Q10, Q12 |
| RG22 | Dans le tableau : `presences` = nombre de sessions de la promotion où l'étudiant est présent, toutes sources confondues ; `exercicesDeposes` = nombre de ses exercices déposés ; `relecturesEnAttente` = nombre de relectures qui lui sont assignées et non rendues | Q11, Q16 |
| RG23 | Le lien d'un exercice peut être remplacé par son auteur tant qu'aucun de ses relecteurs n'a commencé la relecture : ensuite `409 RELECTURE_COMMENCEE` | Q13 |
| RG24 | Un étudiant ne dépose un exercice que pour une session de sa propre promotion : sinon `400 ETUDIANT_HORS_PROMOTION` | Hypothèse (section 7) |
| RG25 | La note retenue d'un exercice est la moyenne des notes rendues par ses relecteurs : la moyenne des deux quand les deux sont rendues ; la seule note rendue, **marquée provisoire**, en attendant l'autre ; aucune note sans relecture rendue. Elle peut ne pas être entière (13,5) : RG3 porte sur la note de chaque relecteur | Enveloppe de l'étape 3 |
| RG26 | Un exercice entièrement relu avant le passage à deux relecteurs garde sa relecture unique, définitive et non provisoire ; tout exercice encore en attente passe à deux relecteurs | Enveloppe (« à partir de maintenant »), section 7 |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| **Le trou que personne n'a vu — qui rend la relecture ?** Le contrat imposé prévoit `403 AUTO_RELECTURE` sur `POST /api/relectures/{id}`, mais la requête ne contient que `{ note, commentaire }` : l'API ne sait pas qui appelle, donc elle ne peut pas détecter une auto-relecture | Aucune question ne le couvre. Q1 : pas de mot de passe, l'étudiant choisit son nom | L'appelant se désigne par l'en-tête **`X-Etudiant-Id`**, identité déclarative cohérente avec Q1. Le front l'envoie toujours. L'en-tête est facultatif : sans lui, la relecture est attribuée au relecteur assigné, pour qu'une requête conforme au seul corps imposé fonctionne | RG2, RG16 ; en-tête ajouté au contrat |
| `POST /api/presences` reçoit un code, pas une session : comment retrouver la session, et que répondre au code d'une autre promotion ? | Hypothèse | Le code est unique pour toujours (contrainte d'unicité en base) : un code expiré désigne encore sa session, on répond donc `410` et non `400`. Le code d'une autre promotion est traité comme inconnu | RG5, RG6 |
| Q7 : le relecteur est tiré parmi les présents. Et si l'auteur est seul présent, ou si personne ne l'est encore au moment du dépôt ? | Hypothèse | L'exercice reste `DEPOSE` et reçoit un relecteur dès qu'un étudiant éligible devient présent | RG15, état `DEPOSE` en D4 |
| « La fin de la session » (Q3, Q10, Q12) n'est jamais définie : aucune heure de fin n'est demandée | Hypothèse | Pas d'heure de fin. Pour l'étudiant, la fenêtre de présence est celle du code (15 min). La clôture est une action explicite du formateur (EF10) | Colonne `cloturee_at`, opération `POST /api/sessions/{id}/cloture` |
| Un étudiant absent peut-il déposer un exercice ? | Q12 autorise le dépôt après la séance ; Q7 réserve la relecture aux présents | Oui : l'exercice est un travail, pas une présence. Il ne sera en revanche relecteur d'aucun exercice de cette session, et ne dépose que pour une session de sa promotion | RG24 |
| Le hasard pur peut confier 5 relectures au même étudiant et aucune à un autre | Q7 : « au hasard », rien sur l'équité | Tirage au hasard parmi les éligibles les moins chargés dans la session : c'est toujours le système qui choisit, au hasard | RG14 |
| Q13 : « tant que personne n'a commencé à le relire ». Le début d'une relecture n'est pas observable | Hypothèse | Le relecteur clique « Commencer la relecture », ce qui lui révèle le lien et horodate `commencee_at` ; le lien n'est plus remplaçable ensuite | RG23, état `EN_COURS_DE_RELECTURE` |
| Q4 : bloquer qui (l'étudiant ou le poste) ? Quelles erreurs comptent ? Avec quel code HTTP, absent du contrat ? | Q4 : 5 erreurs, 2 minutes | Par étudiant, seule identité connue (Q1). Seules les réponses `CODE_INCONNU` comptent, pas un code expiré ni une présence en double. Code `429 TROP_DE_TENTATIVES`, ajouté au contrat | RG7, table `blocage_code` |
| Qui crée les promotions et les étudiants ? | Aucune question | Hors périmètre : les données de démonstration les créent au démarrage | Section 3, exclusions |
| Le formateur a-t-il un compte ? | Q1 : pas de mot de passe | Aucun compte formateur : l'écran formateur est accessible sans identification. Risque accepté | Section 2 |
| Moyenne : quel arrondi, et quelle valeur pour un étudiant sans note ? | Contrat : `moyenne` nullable | Arrondi à 2 décimales ; `null` s'il n'a reçu aucune note ; le front affiche « — » | RG19 |
| Une relecture peut-elle être rendue après la clôture ? | Q10 lie les corrections à la clôture, mais Q10 est écartée (voir contradictions) | Oui : sinon un exercice en attente le resterait pour toujours, ce que Q11 cherche justement à éviter | RG21 |
| `POST /api/sessions` avec une promotion inexistante : quel code ? Le contrat ne prévoit que `400` | Hypothèse | `400 PROMOTION_INCONNUE` : on reste dans les codes prévus pour l'opération imposée | Contrat |
| Quel identifiant dans `POST /api/relectures/{id}` ? Aucune opération imposée ne renvoie un identifiant de relecture | Q6 : un seul relecteur par exercice | ~~Relation 1–1 : la relecture partage l'identifiant de son exercice.~~ **Remplacée à l'étape 3** : avec deux relecteurs, une relecture a son propre identifiant ; le relecteur le lit dans `GET /api/etudiants/{id}/relectures`, qui donne aussi `exerciceId` | D2, migration V2 |
| Le commentaire peut-il être vide ? | Le contrat le déclare obligatoire | Obligatoire, de 1 à 2000 caractères | RG17 |
| Qu'est-ce qu'un lien valide ? | Contrat : `format: uri` | URL absolue en `http(s)` de 500 caractères au plus ; son contenu n'est pas vérifié | RG11 |
| **Étape 3** — Que deviennent les exercices déjà relus par un seul pair avant le changement ? | Enveloppe : « à partir de maintenant » | Ils gardent leur relecture unique, définitive (colonne `relecteurs_requis` = 1) ; les exercices encore en attente passent à deux relecteurs | RG26, migration V2 |
| **Étape 3** — Quand un exercice est-il `RELU` avec deux relecteurs ? | Hypothèse | Quand ses deux relectures sont rendues. Avant, il reste `EN_COURS_DE_RELECTURE` dès qu'une relecture est commencée ou rendue | D4 |
| **Étape 3** — La moyenne du tableau compte-t-elle les notes provisoires ? | Enveloppe : « on affiche sa note en attendant, mais marquée comme provisoire » | Oui, et la moyenne est alors signalée provisoire (champ `moyenneProvisoire`) | RG19, contrat |
| **Étape 3** — Bug #56 : deux présences simultanées pouvaient assigner le même exercice et perdre une présence | Enveloppe, point 1 | Une présence ou un dépôt verrouille sa session : les écritures d'une même session s'enregistrent l'une après l'autre | D3, section 8 |
| Fuseau horaire des dates | Hypothèse | Instants stockés et échangés en UTC (ISO-8601), affichés à l'heure locale | ENF7 |
| Questions sans effet sur le produit | Q10 ; Q1 | Q10 est écartée par la contradiction ci-dessous. Q1 retire l'authentification du périmètre plus qu'elle n'ajoute une fonctionnalité | Section 3 |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| **Q10** « un relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session » contre **Q15** « une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir » | **Q15 : une relecture rendue est définitive (RG18). Q10 est écartée.** | 1) Le contrat imposé tranche déjà : `POST /api/relectures/{id}` prévoit `409 RELECTURE_DEJA_RENDUE`, donc un second envoi est un conflit, pas une correction ; retenir Q10 obligerait à contourner une opération imposée. 2) Q15 est la réponse la plus explicite, et le client la justifie (« plus honnête pour tout le monde »). 3) Une note corrigeable après coup affaiblit la relecture entre pairs, cœur du besoin. Conséquence : la clôture ne fige plus les notes (RG21) |
| **Q16** « sa présence *à chaque session* » contre le contrat imposé, où `presences` est un **entier** | Le contrat est respecté à la lettre : `GET /api/tableau` renvoie un nombre. Le détail session par session passe par une opération distincte, `GET /api/tableau/presences` (EF14, Could) | Le contrat est non négociable (B2) ; le détail répond à Q16 sans modifier l'opération imposée |
| La demande initiale (« présence et moyenne des notes par étudiant ») contre **Q16** (quatre indicateurs) | Q16 | Elle est plus précise, plus récente, et le contrat imposé reprend ses quatre indicateurs |
| **Q13** « on peut remplacer le lien » contre le contrat : `POST /api/exercices` répond `409 EXERCICE_DEJA_DEPOSE` au second dépôt | Le dépôt (`POST`) n'a lieu qu'une fois ; le remplacement est une opération distincte, `PUT /api/exercices/{id}` (EF11, RG23) | Le `409` imposé interdit le double dépôt, pas la modification du lien |
| **Q6** « un seul relecteur » contre l'**enveloppe de l'étape 3** « chaque exercice est relu par deux pairs différents » | L'enveloppe : deux relecteurs, note retenue = moyenne des deux, provisoire tant qu'une seule est rendue (RG13, RG25) | C'est la demande la plus récente du client, motivée par un usage réel (« quand il ne rend rien, l'étudiant n'a aucune note ») |
| **Q3** « pas de présence après la fin de la session » contre **Q14** « le formateur ajoute une présence à la main » | Q3 s'applique à l'étudiant ; Q14 est l'exception du formateur, possible jusqu'à la clôture (RG9) | Le cas de Q14 (un souci de téléphone) se règle justement après la fenêtre de 15 minutes |

## 8. Contraintes techniques

**Imposées par le sujet :**

| Réf | Contrainte | Comment je la respecte |
|---|---|---|
| B1 | Java 17 ou plus, Maven, wrapper `mvnw` commité | Java 21, Spring Boot 4.1, `mvnw` et `.mvn/wrapper/` versionnés |
| B2 | Le contrat `api/contrat.yaml` est respecté à la lettre : chemins, verbes, codes de statut, format d'erreur | Les 5 opérations imposées sont inchangées ; les ajouts sont marqués dans le fichier ; tests d'intégration sur les codes |
| B3 | Séparation contrôleur / service / repository, aucune requête dans un contrôleur, aucune entité JPA exposée en JSON | Paquets `controller`, `service`, `repository`, `entity`, `dto` ; les contrôleurs ne manipulent que des DTO (records Java) |
| B4 | Validation des entrées et gestion centralisée des erreurs (`@RestControllerAdvice`), jamais de stack trace | Bean Validation sur les DTO, un `@RestControllerAdvice` unique, et un contrôleur `/error` qui renvoie lui aussi `{ code, message }` |
| B5 | Schéma versionné par Flyway ou Liquibase, `ddl-auto=update` interdit hors tests | Flyway, `ddl-auto=validate` partout |
| B6 | Un test unitaire sur une règle métier réelle et un test d'intégration sur un endpoint, sur un poste vierge sans base locale | Tests unitaires sur RG1, RG2, RG3, RG5, RG11, RG14, RG15 et RG19 ; tests d'intégration MockMvc sur PostgreSQL 16 démarré par Testcontainers, aucune base installée |
| F1 | Framework déclaré et justifié en une ligne dans le README, build qui passe | React + Vite + TypeScript ; `npm run build` exécuté par la CI |
| F2 | Trois écrans : formateur, étudiant, relecteur | Routes `/formateur`, `/etudiant`, `/relecteur` |
| F3 | Appels API dans une couche dédiée, états de chargement et d'erreur gérés, aucune règle métier dupliquée | Dossier `src/api/`, seul à appeler `fetch` ; moyenne, statuts et contrôles lus depuis l'API |

**Que je m'impose :**
- **Base de données :** PostgreSQL 16, seule base du projet : l'application la reçoit de `docker compose`, les tests d'un conteneur jetable démarré par Testcontainers. H2 a été retiré (#63) : les tests vérifient le comportement de la base de production, verrous compris (#56).
- **Gestion des migrations :** Flyway, un fichier `V<n>__<description>.sql` par changement de schéma, jamais de modification d'une migration déjà poussée (V1 garde donc son commentaire d'origine, qui mentionne H2).
- **Stratégie de tests :** tests unitaires JUnit sur les règles de gestion pures (heure injectée par une `Clock`, hasard par un `Random` à graine fixe) ; tests d'intégration `@SpringBootTest` + MockMvc sur chaque code HTTP du contrat ; chaque test cite la `RGx` qu'il prouve.
- **Concurrence :** une présence ou un dépôt verrouille la ligne de sa session (`SELECT ... FOR UPDATE`) le temps de sa transaction, ce qui corrige le bug #56 ; les contraintes d'unicité restent le dernier rempart (ENF3).
- **Démarrage :** `docker compose up --build` construit le backend et le front dans des conteneurs ; rien d'autre à installer que Docker.
- **Intégration continue :** GitHub Actions lance `./mvnw verify`, `npm run lint` et `npm run build` à chaque pull request ; ces vérifications sont obligatoires pour fusionner dans `develop` et `main`.
- **Git :** une branche par issue créée depuis `develop`, une pull request par branche, fusion sans squash pour garder les commits atomiques ; le dernier commit de la branche ferme l'issue avec `Closes #n`. `main` et `develop` sont protégées : aucune écriture directe. `develop` est publiée dans `main` à chaque jalon.

## 9. Livrables

- Dépôt public `kfokam48-epreuve-KF48-YAO-196`, branche `main` :
  - `docs/CAHIER_DES_CHARGES.md`, ce document, tenu à jour après l'étape 3 ;
  - `docs/JOURNAL.md`, une entrée par étape ;
  - `docs/diagrammes/` : D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 états-transitions d'un exercice (bonus), en Mermaid ;
  - `api/contrat.yaml` : les 5 opérations imposées et les opérations ajoutées ;
  - `backend/` : API Spring Boot, migrations Flyway, tests ;
  - `frontend/` : application React, trois écrans ;
  - `docker-compose.yml`, `README.md` d'installation testé depuis un clone vierge, `CHANGELOG.md` ;
  - backlog en issues priorisées, pull requests liées aux issues, trois commits `[JALON]`.
- `SOUMISSION.md`, téléversé sur la plateforme avant 18h00.

## 10. Démarche prévue

| Étape | Créneau visé | Ce que je vise | Fin de l'étape |
|---|---|---|---|
| 1. Analyser, spécifier, concevoir | 10h15 – 11h30 | Ce cahier des charges, les diagrammes D1 à D4, le backlog en issues, le contrat complété et figé | Entrée de journal, puis `[JALON] analyse` poussé |
| 2. Construire la première version | 11h30 – 14h30 | Les stories **Must** uniquement (EF1 à EF7), une branche et une PR par issue | Entrée de journal, puis `[JALON] v0.1` poussé |
| 3. Ouvrir l'enveloppe | 14h30 – 16h00 | Enveloppe demandée au surveillant une fois `[JALON] v0.1` poussé. Issues ouvertes avant de coder, bug reproduit par un test, migration versionnée, contrat mis à jour, re-priorisation écrite, correctif et évolution sur deux branches séparées, cahier et diagrammes mis à jour dans un commit qui le dit | Entrée de journal |
| 4. Livrer la version finale | 16h00 – 17h00 | Les Should restants, `CHANGELOG.md`, README testé depuis un clone vierge, backlog restant trié | Entrée de journal, puis `[JALON] v1.0` poussé |
| 5. Soumettre | 17h00 – 17h30 | Hash complet relevé, liens vérifiés en navigation privée, `SOUMISSION.md` téléversé | 30 min de marge avant 18h00 |

**Si je prends du retard :** je coupe d'abord le Could (EF14), puis les Should dans l'ordre EF13, EF12, EF11. Je ne coupe jamais les tests B6, le journal, le README, ni la mise à jour de l'analyse après l'étape 3. Toute issue coupée reste ouverte dans le backlog avec sa priorité.

**Re-priorisation de l'étape 3.** Le client a signalé un bug (#56) et demandé la double relecture (#58, #59, #60), un Must qui arrive tard et touche la base, le contrat et trois écrans. Le correctif et l'évolution sont traités sur des branches et des pull requests séparées. Pour livrer le changement testé avant 18h00, **six stories sortent du périmètre de la v1.0** : EF8 (#10), EF9 (#11), EF10 (#12), EF11 (#13), EF13 (#15) et EF14 (#16). Pourquoi celles-là : aucune n'est nécessaire à la double relecture, et chacune a un repli acceptable (le code expire en 15 minutes ; le tableau montre les relectures en attente ; le dépôt reste ouvert sans clôture). EF12 (#14) passe au contraire de Should à Must : sans elle, l'étudiant ne verrait pas sa note provisoire.

**Flux Git par issue :** issue → branche `feat/<n>-<sujet>` (ou `fix/<n>-<sujet>`) créée depuis `develop` → commits atomiques `type(portée): message` citant les `RGx` concernées, le dernier avec `Closes #n` → pull request vers `develop` → CI verte → fusion. À chaque jalon, pull request de publication `develop` vers `main`.

**Definition of Done — une issue est terminée quand :**
- chaque critère d'acceptation de l'issue est vérifié, par un test ou à la main ;
- les règles de gestion citées par l'issue sont couvertes par au moins un test ;
- les opérations touchées respectent `api/contrat.yaml` : chemins, verbes, codes de statut et format d'erreur ;
- `./mvnw verify` et `npm run build` passent (CI verte) ;
- aucun fichier généré et aucun secret n'est commité ;
- la pull request est liée à l'issue et fusionnée dans `develop`, et l'issue est fermée par un commit `Closes #n` ;
- le cahier des charges et les diagrammes sont à jour si l'issue en change le contenu.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25/09/2026, étape 1 | Version initiale |
| 2.1 | 25/09/2026, étape 3 | Une seule base : PostgreSQL. H2 retiré, les tests tournent sur PostgreSQL 16 par Testcontainers (#63) : section 8, B6, ENF6 |
| 2 | 25/09/2026, étape 3 | **Conséquence du changement de besoin de l'enveloppe** : deux relecteurs par exercice. RG13 remplace Q6 ; RG14, RG15, RG16, RG18, RG19, RG20, RG23 adaptées ; RG25 (note retenue, provisoire) et RG26 (exercices déjà relus) ajoutées ; EF5, EF6, EF7 réécrites, EF12 promue Must ; section 7 : contradiction Q6 / enveloppe, décision « id partagé » remplacée, trois zones d'ombre et le bug #56 ; section 8 : verrou de session ; sections 3 et 10 : six stories sorties du périmètre de la v1.0 et pourquoi |
| 1.1 | 25/09/2026, étape 2 | Précisions de l'examinateur : l'épreuve Git est supprimée, l'épreuve compte cinq étapes (section 10, second dépôt retiré de la section 9) ; l'enveloppe se demande au surveillant ; « issue » remplace « ticket ». Choix technique précisé : PostgreSQL est la seule base de l'application, H2 ne sert qu'aux tests (section 8) |
