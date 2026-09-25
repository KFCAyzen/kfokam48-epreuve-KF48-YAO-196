import { requete } from './http'
import type { RelectureAssignee, RelectureRendue } from './types'

// L'appelant se désigne par l'en-tête X-Etudiant-Id (identité déclarative, Q1).
const appelant = (etudiantId: number) => ({ 'X-Etudiant-Id': String(etudiantId) })

export function listerRelectures(etudiantId: number): Promise<RelectureAssignee[]> {
  return requete<RelectureAssignee[]>(`/api/etudiants/${etudiantId}/relectures`)
}

export function commencerRelecture(id: number, etudiantId: number): Promise<RelectureAssignee> {
  return requete<RelectureAssignee>(`/api/relectures/${id}/debut`, { methode: 'POST', entetes: appelant(etudiantId) })
}

/** Opération imposée : POST /api/relectures/{id}. */
export function rendreRelecture(id: number, etudiantId: number, note: number | null, commentaire: string): Promise<RelectureRendue> {
  return requete<RelectureRendue>(`/api/relectures/${id}`, {
    methode: 'POST',
    corps: { note, commentaire },
    entetes: appelant(etudiantId),
  })
}
