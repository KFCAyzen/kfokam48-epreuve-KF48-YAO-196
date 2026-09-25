# D3 — Séquence : marquer sa présence

Le cas nominal et tous les cas d'erreur de `POST /api/presences`, dans l'ordre où le service les vérifie. Chaque code HTTP est celui de `api/contrat.yaml`. Chaque exception métier est traduite par `GestionnaireErreurs` (`@RestControllerAdvice`) au format imposé `{ code, message }`.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front
    participant API as PresenceController
    participant S as PresenceService
    participant DB as Repositories / base
    participant G as GestionnaireErreurs

    E->>F: choisit son nom dans la liste (EF3) puis saisit le code
    F->>API: POST /api/presences { code, etudiantId }
    alt code ou etudiantId absent
        API-->>G: MethodArgumentNotValidException
        G-->>F: 400 { code: "CHAMP_MANQUANT", message }
    else corps valide
        API->>S: marquer(code, etudiantId)
        S->>DB: charger l'étudiant et son blocage
        alt étudiant inexistant
            S-->>G: EtudiantInconnuException
            G-->>F: 400 { code: "ETUDIANT_INCONNU", message }
        else bloqué : 5 codes faux, moins de 2 min (RG7)
            S-->>G: TropDeTentativesException
            G-->>F: 429 { code: "TROP_DE_TENTATIVES", message }
        else non bloqué
            S->>DB: chercher la session par code
            S->>DB: verrouiller la session (SELECT ... FOR UPDATE, correctif #56)
            alt aucune session de sa promotion avec ce code (RG6)
                S->>DB: echecs_consecutifs + 1, blocage 2 min au 5e (RG7)
                S-->>G: CodeInconnuException
                G-->>F: 400 { code: "CODE_INCONNU", message }
            else code expiré : maintenant > expirationAt (RG1)
                S-->>G: CodeExpireException
                G-->>F: 410 { code: "CODE_EXPIRE", message }
            else session clôturée (RG9)
                S-->>G: SessionClotureeException
                G-->>F: 410 { code: "SESSION_CLOTUREE", message }
            else étudiant déjà présent à cette session (RG4)
                S-->>G: DejaPresentException
                G-->>F: 409 { code: "DEJA_PRESENT", message }
            else cas nominal
                S->>DB: INSERT presence, source = ETUDIANT (RG8)
                S->>DB: remise à zéro de echecs_consecutifs (RG7)
                S->>S: compléter les relecteurs manquants des exercices de la session (RG15)
                S-->>API: PresenceDto
                API-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
            end
        end
    end
    F-->>E: affiche « Présence enregistrée » ou le message de l'erreur
```

## Correspondance avec le contrat

| Branche | Code HTTP | Code d'erreur | Déclaré dans `api/contrat.yaml` | Règle |
|---|---|---|---|---|
| Cas nominal | `201` | — | oui, imposé | RG8 |
| Code ou `etudiantId` absent | `400` | `CHAMP_MANQUANT` | oui, `400` imposé | — |
| Étudiant inexistant | `400` | `ETUDIANT_INCONNU` | oui, `400` imposé | — |
| Code inconnu, ou code d'une autre promotion | `400` | `CODE_INCONNU` | oui, imposé | RG6 |
| Étudiant bloqué | `429` | `TROP_DE_TENTATIVES` | oui, **ajouté** pendant l'analyse (Q4) | RG7 |
| Code expiré | `410` | `CODE_EXPIRE` | oui, imposé | RG1 |
| Session clôturée | `410` | `SESSION_CLOTUREE` | oui, `410` imposé | RG9 |
| Déjà présent | `409` | `DEJA_PRESENT` | oui, imposé | RG4 |

**Blocage après 5 codes faux (RG7, `429`)** : sorti du périmètre de la v1.0 à l'étape 3 (#10) ; la branche reste décrite pour la version suivante.

**Correctif #56.** La session est verrouillée avant les vérifications : deux présences simultanées de la même session s'enregistrent l'une après l'autre, et la seconde ne peut plus assigner le même exercice que la première.

Le compteur d'échecs est enregistré dans une transaction séparée : l'exception `CodeInconnuException` n'annule pas l'incrément (RG7).
