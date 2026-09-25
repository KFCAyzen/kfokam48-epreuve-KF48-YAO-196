import { requete } from './http'
import type { SessionOuverte, SessionResume } from './types'

/** Opération imposée : POST /api/sessions. */
export function ouvrirSession(titre: string, promotionId: number): Promise<SessionOuverte> {
  return requete<SessionOuverte>('/api/sessions', { methode: 'POST', corps: { titre, promotionId } })
}

export function listerSessions(promotionId: number): Promise<SessionResume[]> {
  return requete<SessionResume[]>(`/api/promotions/${promotionId}/sessions`)
}
