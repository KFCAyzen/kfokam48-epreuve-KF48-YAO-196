# Journal de bord — KF48-YAO-196

> Une entrée **par étape**, écrite **au moment où tu la termines**, pas à la fin de la journée.
> Trois lignes suffisent. Un journal rédigé d'un bloc juste avant de soumettre se repère
> immédiatement dans l'historique Git et ne compte pas.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que tu viens de terminer
- **Bloqué** — ce qui t'a coûté du temps, et combien
- **IA** — ce que tu lui as demandé, et **comment tu as vérifié sa réponse**

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges (14 exigences fonctionnelles, 9 non fonctionnelles, 24 règles de gestion, 17 zones d'ombre, 1 contradiction franche et 4 tensions tranchées), les quatre diagrammes en Mermaid (D4 en bonus), 19 issues créées (16 stories Must/Should/Could, la CI, le contrat et la clôture d'étape), contrat d'API complété (5 opérations imposées intactes, 11 ajoutées), `develop` créée et `main`/`develop` protégées, commit `[JALON] analyse` poussé.

**Bloqué :** 20 min d'environnement : seul Java 8 était installé, j'ai installé le JDK 21 ; et mon premier commit poussé contenait une ligne d'attribution à l'IA, j'ai préféré supprimer et recréer le dépôt plutôt que de réécrire `main`. 10 min sur la contradiction Q10 / Q15, tranchée en faveur de Q15 : le contrat imposé renvoie `409 RELECTURE_DEJA_RENDUE`, une note rendue ne peut donc pas être corrigée. 15 min sur la lisibilité de D1 et D4.

**IA :** Claude (Claude Code) a rédigé le cahier des charges, les diagrammes, les issues et les ajouts au contrat à partir de mes consignes. Vérifié ainsi :
- **cahier des charges :** comparé titre par titre et colonne par colonne avec `modeles/CAHIER_DES_CHARGES.md`. Sa première version ne respectait pas la numérotation du modèle (EF1 = présence, RG1 à RG3) : réalignée dans un commit dédié ;
- **diagrammes :** rendus dans un navigateur avec Mermaid 11 (4/4 sans erreur) et relus sur capture. Deux «extend» de D1 étaient dans le mauvais sens : corrigés avant commit ;
- **contrat :** validé comme OpenAPI 3.0, puis un script a comparé les 5 opérations imposées avec l'original : aucun corps, paramètre ni code retiré. J'ai aussi vérifié que chaque issue n'utilise que des opérations du contrat.

---

## Étape 2 — Première version

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
