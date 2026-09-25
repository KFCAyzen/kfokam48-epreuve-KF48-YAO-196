import { requete } from './http'
import type { ExerciceAuteur, ExerciceDepose } from './types'

/** Opération imposée : POST /api/exercices. */
export function deposerExercice(sessionId: number, etudiantId: number, lien: string): Promise<ExerciceDepose> {
  return requete<ExerciceDepose>('/api/exercices', { methode: 'POST', corps: { sessionId, etudiantId, lien } })
}

/** Les exercices de l'étudiant, note retenue comprise (EF12). */
export function listerMesExercices(etudiantId: number): Promise<ExerciceAuteur[]> {
  return requete<ExerciceAuteur[]>(`/api/etudiants/${etudiantId}/exercices`)
}
