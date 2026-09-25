# 04 — Formateur · Présence par session

Frame : [Figma, nœud 1:325](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-325) · Issue #30 · Écran livré par #16 · Route `/formateur`, onglet « Présence par session »

Le formateur voit, pour chaque étudiant et chaque session, s'il était présent et par quel moyen.

## Contenu

| Zone | Ce qui est affiché | Source |
|---|---|---|
| En-têtes de colonnes | « S1 » à « S6 » (rang de la session) et la date | `GET /api/tableau/presences` → `sessions[].ouvertureAt` |
| Cases | Pastille pleine : présent par le code ; pastille creuse : ajouté par le formateur ; tiret : absent | `etudiants[].presences[].source` : `ETUDIANT`, `FORMATEUR`, `null` |
| Total | Nombre de présences de l'étudiant | `GET /api/tableau` → `presences`, déjà chargé par l'onglet « Récapitulatif » |
| Légende | Les trois symboles et leur sens (RG8) | — |

## États

| État | Ce que fait l'écran |
|---|---|
| `200` | Grille telle que décrite |
| Promotion sans session | « Aucune session pour cette promotion. » |
| `404 PROMOTION_INCONNUE` | `message` de l'API à la place de la grille |

## Décisions

- **Colonne « Total » lue dans l'API.** Le total vient de `presences` de `GET /api/tableau` (RG22) ; le front ne compte pas les cases (F3).
- **Colonne S6 alignée sur l'écran 02**, qui fait foi pour S6. Corrections : Kamga, Fabrice devient « ajouté par le formateur » ; Mbarga, Aïcha et Nkoulou, Brice deviennent « présent, par le code » ; Tagne, Joël et Yomba, Sandrine deviennent absents.
- **Totaux corrigés** : Nkoulou 6, Tagne 1, Yomba 5 ; les autres lignes ne changent pas. La colonne somme à 58, pour 9 présents à S6.
- **S1 à S5 ne changent pas** et font foi pour les écrans 01 et 03 : présents par session S1 10, S2 10, S3 10, S4 9, S5 10.
