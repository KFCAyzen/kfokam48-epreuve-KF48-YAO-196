# 02 — Formateur · Détail de session

Frame : [Figma, nœud 1:96](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-96) · Issue #28 · Écran livré par #11, #12, #15 · Route `/formateur`

Le formateur voit qui est présent et par quel moyen, où en sont les exercices déposés, ajoute un absent à la main et clôture la session.

## Contenu

Toutes les données viennent de `GET /api/sessions/{id}` (`SessionDetail`).

| Zone | Ce qui est affiché | Champ du contrat |
|---|---|---|
| En-tête | Titre, code, heure d'ouverture, heure d'expiration du code, bouton « Clôturer la session » | `titre`, `code`, `ouvertureAt`, `expirationAt`, `clotureeAt` |
| Présents | « 9 sur 12 », puis une ligne par présent : nom, heure ; mention « ajouté par le formateur » si la source est le formateur (RG8) | `presents`, `presences[].nom`, `presences[].marqueeAt`, `presences[].source` |
| Ajout à la main | Liste des étudiants de la promotion absents de la session, bouton « Ajouter » | `GET /api/promotions/{id}/etudiants` moins `presences[].etudiantId` ; envoi `POST /api/sessions/{id}/presences` |
| Exercices | Compteur « 5 exercices en attente », puis une ligne par exercice : auteur et statut | `exercicesEnAttente`, `exercices[].nom`, `exercices[].statut` |

Note affichée sous l'ajout : la présence peut être ajoutée à la main jusqu'à la clôture, même après l'expiration du code (RG9). Note sous les exercices : un exercice sans relecteur en recevra un dès qu'un étudiant éligible marquera sa présence (RG15).

Les statuts sont libellés selon le tableau du [README](README.md#libellés-des-statuts-dexercice) ; les exercices non relus sont mis en évidence (EF13, Q11).

## États

| État | Ce que fait l'écran |
|---|---|
| Session ouverte | Tel que dessiné |
| Confirmation de clôture | « Clôturer définitivement cette session ? » avec « Clôturer » et « Annuler », car la clôture ne se défait pas (RG21) |
| Session clôturée (`clotureeAt` renseigné) | La mention « Clôturée le … à … » remplace le bouton ; le bloc d'ajout à la main disparaît ; les exercices restent listés, les relectures assignées pouvant encore être rendues (RG21) |
| `201` à l'ajout | La liste des présents est rechargée ; l'étudiant ajouté porte « ajouté par le formateur » |
| `400 ETUDIANT_HORS_PROMOTION`, `409 DEJA_PRESENT`, `409 SESSION_CLOTUREE` | `message` de l'API sous le bloc d'ajout |
| `409 SESSION_DEJA_CLOTUREE` à la clôture | `message` de l'API, puis la session est rechargée dans l'état clôturé |
| `404 SESSION_INCONNUE` | `message` de l'API à la place de la page |

## Décisions

- **Compteur des exercices en attente.** La frame affiche « 3 relectures en attente » et ne compte pas l'exercice en cours de relecture. Le compteur affiche le champ `exercicesEnAttente` de l'API, qui compte les exercices non relus (`DEPOSE`, `EN_ATTENTE_RELECTURE`, `EN_COURS_DE_RELECTURE`, Q11) : 5 dans l'exemple de la frame. Le libellé devient « exercices en attente ». Cette décision remplace celle de l'issue #28 (« 4 relectures ») : le front ne recompte rien (F3).
- **Heure de dépôt retirée.** La colonne « Déposé » n'a pas de champ dans `SessionDetail.exercices` ; elle est retirée plutôt que d'ajouter un champ au contrat figé (B2).
- **Référence pour S6.** Cette frame fait foi pour les présents de S6 (Ateba, Djomo, Fotso, Mbarga, Ngo Biyong, Nkoulou, Tchoumi, Wamba par le code ; Kamga ajouté par le formateur). Le registre de l'écran 04 est aligné sur elle.
