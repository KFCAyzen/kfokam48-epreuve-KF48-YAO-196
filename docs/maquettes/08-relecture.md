# 08 — Relecteur · Relectures assignées et notation

Frame : [Figma, nœud 1:617](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-617) · Issue #34 · Écran livré par #8 · Route `/relecteur`

Le relecteur voit les relectures qui lui sont assignées, en commence une, puis rend une note entière sur 20 et un commentaire (EF6).

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| Identité | Nom choisi, promotion, lien « Changer d'identité » (écran 05) | Identité conservée |
| Relectures assignées | Une ligne par relecture : « Exercice n° 0412 », titre de la session, statut ; note si la relecture est rendue | `GET /api/etudiants/{id}/relectures` → `id`, `sessionTitre`, `statut`, `note` |
| Action | Bouton « Commencer la relecture » sur les relectures « À commencer » | `POST /api/relectures/{id}/debut` |
| Relecture en cours | Lien de l'exercice, champ « Note » sur 20, champ « Commentaire » avec compteur « 212 / 2000 », bouton « Rendre la relecture » | `lien` (non nul une fois commencée) ; envoi `POST /api/relectures/{id}` |

Notes du formulaire : note entière de 0 à 20 (RG3) ; commentaire obligatoire, 2000 caractères au plus (RG17) ; une relecture rendue est définitive (RG18).

Les statuts sont libellés selon le tableau du [README](README.md#libellés-des-statuts-dexercice).

## États

| État | Ce que fait l'écran |
|---|---|
| Aucune relecture assignée | « Aucune relecture ne vous est assignée pour l'instant. » |
| « À commencer » | Seul le bouton « Commencer la relecture » est proposé ; le lien n'est pas affiché, l'API le renvoyant `null` |
| `200` au début | Le formulaire s'ouvre avec le lien de l'exercice |
| `200` au rendu | La ligne passe « Rendue · 14 / 20 » et le formulaire se ferme |
| `400 NOTE_INVALIDE` | `message` de l'API sous le champ « Note » (par exemple pour 21 ou 12,5), saisie conservée |
| `400 COMMENTAIRE_INVALIDE` | `message` de l'API sous le champ « Commentaire » |
| `409 RELECTURE_DEJA_RENDUE` | `message` de l'API ; la liste est rechargée |
| `403 RELECTEUR_NON_ASSIGNE`, `403 AUTO_RELECTURE` | `message` de l'API à la place du formulaire (RG2, RG16) |

## Décisions

- **Heure de début retirée.** « Commencée à 10 h 42 » n'a pas de champ dans `RelectureAssignee` ; la mention est retirée plutôt que d'ajouter un champ au contrat figé (B2). L'incohérence relevée dans l'issue #34 (début avant le dépôt de 10:44 de l'écran 02) disparaît avec elle. Cette décision remplace celle de l'issue #34 (« 10 h 48 »).
- **Titre de session sans rang.** La ligne affiche `sessionTitre` ; le préfixe « S6 · » de la frame n'est pas fourni par l'API.
- **Le compteur de caractères n'est qu'une aide.** Le champ limite la saisie à 2000 caractères, mais c'est l'API qui valide RG3 et RG17.
