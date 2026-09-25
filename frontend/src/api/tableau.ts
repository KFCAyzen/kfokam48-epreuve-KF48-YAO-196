import { requete } from './http'
import type { LigneTableau } from './types'

/** Opération imposée : GET /api/tableau?promotionId= */
export function lireTableau(promotionId: number): Promise<LigneTableau[]> {
  return requete<LigneTableau[]>(`/api/tableau?promotionId=${promotionId}`)
}
