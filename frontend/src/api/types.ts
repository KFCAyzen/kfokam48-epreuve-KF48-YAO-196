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
