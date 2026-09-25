import { requete } from './http'
import type { Presence } from './types'

/** Opération imposée : POST /api/presences. */
export function marquerPresence(code: string, etudiantId: number): Promise<Presence> {
  return requete<Presence>('/api/presences', { methode: 'POST', corps: { code, etudiantId } })
}
