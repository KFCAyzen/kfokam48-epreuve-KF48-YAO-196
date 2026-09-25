# 06 — Étudiant · Marquer sa présence

Frame : [Figma, nœud 1:521](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-521) · Issue #32 · Écran livré par #5, #10 · Route `/etudiant`

L'étudiant saisit le code affiché en salle et comprend, quand le code est refusé, pourquoi.

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| Identité | Nom choisi, promotion, lien « Changer d'identité » (écran 05) | Identité conservée |
| Code | Six cases d'un caractère, saisie en majuscules | — |
| Action | Bouton « Valider ma présence » | `POST /api/presences` |
| Note | Le code est valable 15 minutes après l'ouverture ; en cas de problème, le formateur peut ajouter la présence à la main (RG1, RG9) | — |

## États

La frame ne dessine que le code inconnu. Les autres états sont spécifiés ici, chacun avec le `message` renvoyé par l'API (ENF8).

| Réponse | Ce que fait l'écran |
|---|---|
| `201` | « Présence enregistrée. » remplace le formulaire |
| `400 CODE_INCONNU` | `message` de l'API sous les cases, code conservé pour correction (RG6) |
| `410 CODE_EXPIRE` | `message` de l'API ; la note rappelle que le formateur peut ajouter la présence à la main (RG1, RG9) |
| `409 DEJA_PRESENT` | `message` de l'API, affiché comme une information et non comme une erreur ; le formulaire disparaît (RG4) |
| `410 SESSION_CLOTUREE` | `message` de l'API (RG9) |
| `429 TROP_DE_TENTATIVES` | `message` de l'API ; le bouton reste actif et l'API répond `429` tant que le blocage dure (RG7) |

## Décisions

- **Le front ne compte pas les essais.** La phrase « Encore 3 essais avant un blocage de 2 minutes » de la frame fait partie du `message` rédigé par l'API pour `CODE_INCONNU`. Le front n'a ni compteur ni minuterie : les tenir dupliquerait RG7 (F3). Le contrat ne change pas, le `message` étant un texte libre.
- **Pas de validation de l'alphabet du code côté front.** Les cases limitent la saisie à 6 caractères et la passent en majuscules ; c'est l'API qui dit si le code existe (RG5, RG6).
- **Largeur.** Dessiné en 390 px, l'écran est vérifié à 360 × 640 : six cases et bouton d'au moins 44 px de haut, sans défilement horizontal (ENF1).
