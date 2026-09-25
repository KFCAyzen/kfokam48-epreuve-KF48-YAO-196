import type { StatutExercice } from '../api/types'

// Libellés de docs/maquettes/README.md, « Libellés des statuts d'exercice ».
const COTE_AUTEUR: Record<StatutExercice, string> = {
  DEPOSE: 'Déposé, sans relecteur',
  EN_ATTENTE_RELECTURE: 'En attente de relecture',
  EN_COURS_DE_RELECTURE: 'En cours de relecture',
  RELU: 'Relu',
}

const COTE_RELECTEUR: Record<StatutExercice, string> = {
  DEPOSE: 'Non assigné',
  EN_ATTENTE_RELECTURE: 'À commencer',
  EN_COURS_DE_RELECTURE: 'En cours',
  RELU: 'Rendue',
}

/** Libellé d'un statut vu par le formateur ou l'auteur. */
export function libelleStatut(statut: StatutExercice): string {
  return COTE_AUTEUR[statut]
}

/** Libellé d'un statut vu par le relecteur. */
export function libelleStatutRelecteur(statut: StatutExercice): string {
  return COTE_RELECTEUR[statut]
}
