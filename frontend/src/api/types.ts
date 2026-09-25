// Types des réponses de l'API, recopiés des schémas de api/contrat.yaml.

export type Promotion = {
  id: number
  nom: string
}

export type Etudiant = {
  id: number
  nom: string
  promotionId: number
}

/** Réponse imposée de POST /api/sessions. */
export type SessionOuverte = {
  id: number
  code: string
  ouvertureAt: string
  expirationAt: string
}

/** Schéma « SessionResume ». */
export type SessionResume = SessionOuverte & {
  titre: string
  promotionId: number
  clotureeAt: string | null
  presents: number
  exercicesDeposes: number
  exercicesEnAttente: number
}

/** Réponse imposée de POST /api/presences. */
export type Presence = {
  id: number
  sessionId: number
  etudiantId: number
  source: 'ETUDIANT' | 'FORMATEUR'
}

/** Schéma « StatutExercice » : cycle de vie d'un exercice (diagramme D4). */
export type StatutExercice = 'DEPOSE' | 'EN_ATTENTE_RELECTURE' | 'EN_COURS_DE_RELECTURE' | 'RELU'

/** Réponse imposée de POST /api/exercices. */
export type ExerciceDepose = {
  id: number
  statut: StatutExercice
}
