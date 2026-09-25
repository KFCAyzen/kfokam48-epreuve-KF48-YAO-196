# 05 — Étudiant · Identification

Frame : [Figma, nœud 1:486](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-486) · Issue #31 · Écran livré par #4 · Routes `/etudiant` et `/relecteur`

L'étudiant ou le relecteur se désigne sans mot de passe : il choisit sa promotion, puis son nom dans la liste (EF3, Q1).

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| Titre | « Qui êtes-vous ? » | — |
| Promotion | Liste déroulante des promotions | `GET /api/promotions` |
| Nom | Liste des étudiants de la promotion choisie, triée par nom | `GET /api/promotions/{id}/etudiants` |
| Action | Bouton « Continuer », actif quand un nom est choisi | — |
| Note | Aucun mot de passe n'est demandé ; le choix est conservé sur cet appareil jusqu'à « Changer d'identité » | — |

## États

| État | Ce que fait l'écran |
|---|---|
| Aucune identité conservée | Écran tel que dessiné |
| Identité déjà conservée | L'écran est sauté : l'étudiant arrive sur l'écran 06, le relecteur sur l'écran 08 |
| « Changer d'identité » cliqué | L'identité conservée est oubliée et cet écran réapparaît |
| `404 PROMOTION_INCONNUE` | `message` de l'API sous la liste des promotions |

## Décisions

- **Même écran pour les deux rôles.** Le relecteur est un étudiant de la promotion ; il se désigne par le même écran.
- **Largeur.** Dessiné en 390 px, l'écran est vérifié à 360 × 640 sans défilement horizontal, listes et bouton d'au moins 44 px de haut (ENF1).
- **Conservation de l'identité** dans le stockage local du navigateur : elle ne sert qu'à éviter de se redésigner, l'API reste seule juge de ce que l'étudiant peut faire.
