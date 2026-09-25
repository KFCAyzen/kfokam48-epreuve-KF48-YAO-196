# 03 — Formateur · Tableau de la promotion

Frame : [Figma, nœud 1:204](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-204) · Issue #29 · Écran livré par #9 · Route `/formateur`

Le formateur suit sa promotion : une ligne par étudiant avec les quatre indicateurs demandés par le client.

## Contenu

Toutes les valeurs du tableau viennent de `GET /api/tableau?promotionId=` ; l'écran n'en calcule aucune (EF7, F3).

| Colonne | Champ du contrat | Affichage |
|---|---|---|
| N° | — | Rang de la ligne, dans l'ordre renvoyé (trié par nom) |
| Étudiant | `nom` | Tel quel |
| Présences | `presences` | « 6 / 6 », le dénominateur étant le nombre de sessions de `GET /api/promotions/{id}/sessions` |
| Exercices déposés | `exercicesDeposes` | Tel quel |
| Moyenne / 20 | `moyenne` | Deux décimales avec virgule ; « — » si `null` (RG19) |
| Relectures en attente | `relecturesEnAttente` | Tel quel |

Notes sous le tableau : la moyenne est la moyenne arithmétique des notes reçues sur les exercices relus, arrondie à deux décimales, « — » sans note (RG19) ; les présences comptent toutes les sources, code ou ajout du formateur (RG22).

Deux onglets au-dessus du tableau : « Récapitulatif » (cet écran) et « Présence par session » (écran 04).

## États

| État | Ce que fait l'écran |
|---|---|
| `200` | Tableau tel que décrit |
| Promotion sans étudiant | « Aucun étudiant dans cette promotion. » |
| `404 PROMOTION_INCONNUE`, `400 PARAMETRE_INVALIDE` | `message` de l'API à la place du tableau |

## Décisions

- **Ligne « Promotion » retirée.** La frame termine par une ligne de total (60 / 72, 54, 14,02, 7). `GET /api/tableau` ne la fournit pas, et l'afficher obligerait le front à recalculer une moyenne, ce que F3 interdit. Elle est retirée ; le contrat ne change pas.
- **Présences alignées sur le registre de l'écran 04 corrigé** : Mvondo, Inès 2 / 6 ; Nkoulou, Brice 6 / 6 ; Tagne, Joël 1 / 6 ; Yomba, Sandrine 5 / 6.
- **Moyennes rendues possibles.** Avec des notes entières (RG3) et le nombre d'exercices relus possible, 14,80, 13,25 et 12,60 ne peuvent pas être obtenues. Valeurs retenues : Mbarga 14,75 (4 notes), Nkoulou 13,33 (3 notes), Wamba 12,50 (4 notes).
- **Relectures en attente de Mbarga, Aïcha : 2.** L'écran 08 lui assigne deux relectures non rendues (n° 0412 et n° 0398) ; la frame affichait 0.
