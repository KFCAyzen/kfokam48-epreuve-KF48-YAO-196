# Maquettes des écrans

Les huit écrans des trois rôles (formateur, étudiant, relecteur) sont dessinés dans Figma. Chaque écran a ici sa spécification versionnée, relue en pull request comme le reste de l'analyse.

Fichier Figma : <https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled>

| N° | Écran | Frame Figma | Spécification | Issue | Exigences |
|---|---|---|---|---|---|
| 01 | Formateur · Ouvrir une session | [nœud 1:9](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-9) | [01-ouvrir-session.md](01-ouvrir-session.md) | #27 | EF2 |
| 02 | Formateur · Détail de session | [nœud 1:96](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-96) | [02-detail-session.md](02-detail-session.md) | #28 | EF9, EF10, EF13 |
| 03 | Formateur · Tableau de la promotion | [nœud 1:204](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-204) | [03-tableau-promotion.md](03-tableau-promotion.md) | #29 | EF7 |
| 04 | Formateur · Présence par session | [nœud 1:325](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-325) | [04-presence-par-session.md](04-presence-par-session.md) | #30 | EF14 |
| 05 | Étudiant · Identification | [nœud 1:486](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-486) | [05-identification.md](05-identification.md) | #31 | EF3 |
| 06 | Étudiant · Marquer sa présence | [nœud 1:521](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-521) | [06-marquer-presence.md](06-marquer-presence.md) | #32 | EF1, EF8 |
| 07 | Étudiant · Déposer un exercice, notes reçues | [nœud 1:549](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-549) | [07-exercices-etudiant.md](07-exercices-etudiant.md) | #33 | EF4, EF11, EF12 |
| 08 | Relecteur · Relectures assignées et notation | [nœud 1:617](https://www.figma.com/design/UHhZ3L915j9Las2CxcfPkl/Untitled?node-id=1-617) | [08-relecture.md](08-relecture.md) | #34 | EF6 |

## Règles communes

Ces règles tranchent les écarts entre les frames Figma, le contrat et les contraintes du sujet. Elles valent pour les huit écrans.

1. **La spécification fait foi.** Quand une frame Figma diffère de sa spécification, c'est la spécification qui s'applique. Elle est versionnée et relue en pull request ; le fichier Figma n'est pas modifiable depuis le poste de développement (quota de l'offre Figma Starter atteint).
2. **Un écran n'affiche que ce que le contrat fournit.** Toute donnée de la maquette absente de `api/contrat.yaml` est retirée de l'écran, jamais ajoutée au contrat : le contrat est figé depuis le jalon d'analyse et doit être respecté à la lettre (B2).
3. **Aucune règle métier n'est recalculée par le front** (F3). Moyennes, totaux, compteurs d'attente et blocages viennent de l'API. Le front se limite à formater (dates, décimales), trier et filtrer ce qu'il reçoit.
4. **Les messages d'erreur sont ceux de l'API** (ENF8). La spécification fixe le code attendu et ce que l'écran fait autour ; le texte affiché est le `message` renvoyé au format `{ code, message }`.
5. **Chargement et erreur sont gérés sur chaque écran** (F3), même quand la frame ne les dessine pas : texte « Chargement… » pendant l'appel, `message` de l'API en cas d'échec, sans effacer la saisie en cours.
6. **Les écrans étudiant sont vérifiés à 360 × 640** (ENF1). Les frames 05 et 06 sont dessinées en 390 px ; l'écran réel ne doit avoir aucun défilement horizontal à 360 px, et le champ du code comme les boutons font au moins 44 px de haut.
7. **Les chiffres des maquettes sont illustratifs.** Ils sont rendus cohérents d'un écran à l'autre par les décisions de chaque spécification. L'application affiche ceux de ses données de démonstration.

## Libellés des statuts d'exercice

| `StatutExercice` (contrat) | Libellé côté formateur et auteur | Libellé côté relecteur |
|---|---|---|
| `DEPOSE` | Déposé, sans relecteur | — (non assigné) |
| `EN_ATTENTE_RELECTURE` | En attente de relecture | À commencer |
| `EN_COURS_DE_RELECTURE` | En cours de relecture | En cours |
| `RELU` | Relu | Rendue · note / 20 |

## Données de référence des maquettes

Deux sources font foi pour que les écrans concordent : le registre de la frame 04 pour les sessions S1 à S5, et la liste des présents de la frame 02 pour la session S6, car elle concorde avec la frame 06 où Mbarga, Aïcha marque sa présence par le code.
