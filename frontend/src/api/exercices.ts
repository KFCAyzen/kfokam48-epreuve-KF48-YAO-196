import { requete } from './http'
import type { ExerciceDepose } from './types'

/** Opération imposée : POST /api/exercices. */
export function deposerExercice(sessionId: number, etudiantId: number, lien: string): Promise<ExerciceDepose> {
  return requete<ExerciceDepose>('/api/exercices', { methode: 'POST', corps: { sessionId, etudiantId, lien } })
}
