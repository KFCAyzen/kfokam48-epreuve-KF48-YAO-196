# Changelog

Toutes les évolutions notables du projet, version par version. Chaque ligne renvoie à son issue et à la pull request qui l'a fusionnée dans `develop` ; chaque version est publiée sur `main` par une pull request `develop` → `main` et marquée par un commit `[JALON]`.

## [1.1] — 25/09/2026 (après la v1.0)

### Ajouté
- Documentation Swagger de l'API (#75) : générée depuis le code par springdoc-openapi (`/swagger-ui.html`, `/v3/api-docs`, aussi servie par le front) et Swagger UI du contrat imposé `api/contrat.yaml` dans `docker compose` (port 8090).

## [1.0] — 25/09/2026 (étapes 3 et 4)

Publiée par le commit `[JALON] v1.0`.

### Corrigé
- Deux étudiants qui validaient le code au même instant : la présence de l'un était perdue lorsqu'un exercice attendait un relecteur. Une présence ou un dépôt verrouille maintenant sa session (#56, PR #57). Le bug a été prouvé par un test qui échouait, avant la correction.

### Modifié : double relecture, changement de besoin du client
- Chaque exercice est relu par **deux pairs différents** ; la note retenue est la moyenne des deux, **marquée provisoire** tant qu'un seul a rendu (RG13, RG25).
  - Analyse mise à jour : cahier des charges v2, diagrammes D1 à D4, contrat d'API (#58, PR #62).
  - API : nouvelle migration **V2**, qui conserve les données existantes ; deux relecteurs tirés parmi les présents ; note retenue et `moyenneProvisoire` (#59, PR #65).
  - Écrans : « Mes exercices » avec la note provisoire, relecteur, tableau (#60, PR #66).
- L'étudiant voit sa note retenue et les commentaires, sans le nom des relecteurs (EF12, #14, promue Must).
- Les tests tournent sur PostgreSQL 16 par Testcontainers, comme l'application ; H2 est retiré (#63, PR #64).

### Sorti du périmètre (re-priorisation de l'étape 3)
- Blocage après 5 codes faux (#10), présence ajoutée à la main (#11), clôture d'une session (#12), remplacement du lien (#13), détail d'une session (#15), grille de présence (#16). Ces stories restent dans le backlog ; la raison est écrite dans le cahier des charges (sections 3 et 10) et dans le journal.

### Documentation
- Journal des étapes 3 et 4 (#67, PR #68 ; #69). README et CHANGELOG (#69).

## [0.1] — 25/09/2026 (étape 2)

Publiée par le commit `[JALON] v0.1` (PR #55).

### Ajouté
- Démarrage en une commande par `docker compose` (PostgreSQL, API, front) avec des données de démonstration (#1, PR #24).
- Toute erreur de l'API au format `{ code, message }`, sans stack trace (#2, PR #35).
- Vérification automatique de chaque pull request : tests backend, lint, tests et build du front (#17, PR #25 ; #46, PR #47).
- Le formateur ouvre une session et obtient un code de présence (EF2, #3, PR #44).
- L'étudiant et le relecteur se désignent dans la liste de leur promotion, sans mot de passe (EF3, #4, PR #45).
- L'étudiant marque sa présence avec le code (EF1, #5, PR #48).
- L'étudiant dépose le lien de son exercice (EF4, #6, PR #49).
- Un relecteur est tiré au hasard parmi les présents (EF5, #7, PR #50).
- Le relecteur commence puis rend sa relecture (EF6, #8, PR #51).
- Le formateur consulte le tableau de sa promotion (EF7, #9, PR #53).

### Documentation
- Précisions de l'examinateur appliquées à l'analyse : cinq étapes, « issue » au lieu de « ticket », PostgreSQL (#23, PR #26).
- Spécifications des huit écrans à partir des maquettes (#27 à #34, PR #36 à #43).
- Journal de l'étape 2 et jalon (#52, PR #54).

## Analyse — 25/09/2026 (étape 1)

Publiée par le commit `[JALON] analyse` (PR #22), avant tout code.

- Cahier des charges en dix sections, EF1 à EF14, RG1 à RG24, contradiction Q10 / Q15 tranchée.
- Diagrammes D1 à D4 en Mermaid.
- Contrat d'API complété et figé (#18, PR #20).
- Backlog en issues priorisées ; journal de l'étape 1 (#19, PR #21).
