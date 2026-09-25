# 07 — Étudiant · Déposer un exercice, notes reçues

Frame : [Figma, nœud 1:549](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-549) · Issue #33 · Écran livré par #6, #13, #14 · Route `/etudiant`

L'étudiant dépose le lien de son exercice, le remplace tant que la relecture n'a pas commencé, et lit les notes reçues sans connaître son relecteur.

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| Dépôt | Liste des sessions non clôturées de la promotion, champ « Lien vers l'exercice », bouton « Déposer » | `GET /api/promotions/{id}/sessions` (sessions sans `clotureeAt`) ; envoi `POST /api/exercices` |
| Mes exercices | Une carte par exercice, la plus récente d'abord : titre de la session, statut, lien | `GET /api/etudiants/{id}/exercices` → `sessionTitre`, `statut`, `lien` |
| Remplacement | Bouton « Remplacer le lien » sur les exercices `DEPOSE` ou `EN_ATTENTE_RELECTURE` | Envoi `PUT /api/exercices/{id}` |
| Relecture reçue | Note « 16 sur 20 », commentaire, signature « Relecteur anonyme » | `note`, `commentaire` ; aucune donnée sur le relecteur (RG20) |

Notes du formulaire : lien commençant par `http://` ou `https://`, 500 caractères au plus (RG11) ; un seul exercice par session (RG10) ; le lien peut être remplacé tant que le relecteur n'a pas commencé (RG23) ; l'identité du relecteur n'est jamais communiquée (RG20).

## États

| État | Ce que fait l'écran |
|---|---|
| `201` au dépôt | L'exercice apparaît en tête de « Mes exercices » avec son statut |
| `400 LIEN_INVALIDE`, `400 ETUDIANT_HORS_PROMOTION`, `409 EXERCICE_DEJA_DEPOSE`, `409 SESSION_CLOTUREE` | `message` de l'API sous le formulaire, saisie conservée |
| Remplacement ouvert | Champ prérempli avec le lien actuel, boutons « Enregistrer » et « Annuler » |
| `200` au remplacement | Le nouveau lien s'affiche sur la carte |
| `400 LIEN_INVALIDE`, `403 PAS_AUTEUR` au remplacement | `message` de l'API sur la carte, saisie conservée |
| Relecture commencée (`EN_COURS_DE_RELECTURE`) | Plus de bouton « Remplacer le lien » ; mention « Relecture commencée : le lien ne peut plus être remplacé. » (EF11) |
| `409 RELECTURE_COMMENCEE` | `message` de l'API sur la carte, puis la liste est rechargée : le relecteur a commencé entre-temps |
| Aucun exercice | « Aucun exercice déposé pour l'instant. » |

## Décisions

- **Moyenne retirée de cet écran.** `GET /api/etudiants/{id}/exercices` ne renvoie pas de moyenne. La calculer dans le front est interdit (F3), et la lire dans `GET /api/tableau` exposerait à l'étudiant les résultats de toute sa promotion. EF12 ne demande que la note et le commentaire : la moyenne reste sur l'écran formateur (03). Cette décision remplace celle de l'issue #33 (« 14,75 »).
- **Date de rendu retirée.** `ExerciceAuteur` n'a pas de date de rendu ; « rendue le 23 sept. 2026 » est retiré plutôt que d'ajouter un champ au contrat figé (B2).
- **Titre de session sans rang.** La carte affiche `sessionTitre` ; le préfixe « S6 · » de la frame n'est pas fourni par l'API.
- **Le bouton « Remplacer le lien » suit le statut renvoyé par l'API** ; l'API reste seule juge et répond `409 RELECTURE_COMMENCEE` si la relecture a commencé entre-temps (RG23).
