import { requete } from './http'
import type { Etudiant, Promotion } from './types'

export function listerPromotions(): Promise<Promotion[]> {
  return requete<Promotion[]>('/api/promotions')
}

export function listerEtudiants(promotionId: number): Promise<Etudiant[]> {
  return requete<Etudiant[]>(`/api/promotions/${promotionId}/etudiants`)
}
