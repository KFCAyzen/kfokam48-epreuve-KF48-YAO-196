# 01 — Formateur · Ouvrir une session

Frame : [Figma, nœud 1:9](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-9) · Issue #27 · Écran livré par #3 · Route `/formateur`

Le formateur ouvre une session pour sa promotion et projette en salle le code de présence, avec son heure d'expiration.

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| Formulaire | Titre de la séance, liste des promotions, bouton « Ouvrir la session » | `GET /api/promotions` |
| Code de présence | Les 6 caractères du code en grand, lisibles du fond de la salle | `POST /api/sessions` → `code` |
| Horaires | « Ouverte à 10 h 15 · expire à 10 h 30 » et le temps restant | `ouvertureAt`, `expirationAt` |
| Présences | « 4 présences enregistrées sur 12 », lien « Voir la session » vers l'écran 02 | `GET /api/promotions/{id}/sessions` → `presents` ; effectif = taille de `GET /api/promotions/{id}/etudiants` |
| Sessions précédentes | N°, séance, date, présents, statut | `GET /api/promotions/{id}/sessions` → `titre`, `ouvertureAt`, `presents`, `clotureeAt` |

Notes affichées sous le formulaire : le code comporte six caractères, sans 0, O, 1 ni I, et n'est jamais réutilisé (RG5) ; il expire quinze minutes après l'ouverture (RG1) ; le formateur peut ensuite ajouter une présence à la main jusqu'à la clôture (RG9).

## États

| État | Ce que fait l'écran |
|---|---|
| Aucune session ouverte | Formulaire seul, bouton actif dès qu'un titre et une promotion sont saisis |
| `201` | Le bloc du code remplace le formulaire ; le temps restant décompte jusqu'à `expirationAt` |
| Code expiré | Le code reste affiché, barré, avec « Code expiré à 10 h 30 » ; le lien « Voir la session » reste actif |
| `400 CHAMP_MANQUANT`, `400 PROMOTION_INCONNUE` | `message` de l'API sous le formulaire, saisie conservée |

Le temps restant est un simple affichage de `expirationAt - maintenant` ; l'expiration elle-même est décidée par l'API (RG1).

## Décisions

- **Présents des sessions précédentes.** La frame affiche S2 11, S3 9, S4 10, S5 11, ce qui contredit le registre de l'écran 04. Le registre fait foi pour S1 à S5 : les valeurs retenues sont S2 10 / 12, S3 10 / 12, S4 9 / 12, S5 10 / 12.
- **Statut d'une session précédente** : « Clôturée » si `clotureeAt` est renseigné, « Ouverte » sinon. Aucune autre valeur n'est déduite par le front.
