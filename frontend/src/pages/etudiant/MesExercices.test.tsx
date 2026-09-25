import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { ExerciceAuteur } from '../../api/types'
import { simulerApi } from '../../test/apiSimulee'
import MesExercices from './MesExercices'

function exercice(champs: Partial<ExerciceAuteur>): ExerciceAuteur {
  return {
    id: 1,
    sessionId: 6,
    sessionTitre: 'Conception d’API REST',
    lien: 'https://github.com/ambarga/kf48',
    statut: 'EN_COURS_DE_RELECTURE',
    note: null,
    noteProvisoire: false,
    commentaires: [],
    ...champs,
  }
}

describe('MesExercices (spécification 07, EF12, étape 3)', () => {
  it('affiche la note d’un seul relecteur marquée « provisoire » (RG25)', async () => {
    simulerApi({
      'GET /api/etudiants/11/exercices': { corps: [exercice({ note: 16, noteProvisoire: true, commentaires: ['Bien.'] })] },
    })
    render(<MesExercices etudiantId={11} version={0} />)

    expect(await screen.findByText('16,00')).toBeInTheDocument()
    expect(screen.getByText('provisoire')).toBeInTheDocument()
    expect(screen.getByText('Relecteur anonyme')).toBeInTheDocument()
  })

  it('affiche la moyenne des deux notes, définitive, et les deux commentaires (RG25, RG20)', async () => {
    simulerApi({
      'GET /api/etudiants/11/exercices': {
        corps: [exercice({ statut: 'RELU', note: 12.5, noteProvisoire: false, commentaires: ['Bien.', 'À compléter.'] })],
      },
    })
    render(<MesExercices etudiantId={11} version={0} />)

    expect(await screen.findByText('12,50')).toBeInTheDocument()
    expect(screen.queryByText('provisoire')).not.toBeInTheDocument()
    expect(screen.getAllByText('Relecteur anonyme')).toHaveLength(2)
  })

  it('annonce l’absence d’exercice', async () => {
    simulerApi({ 'GET /api/etudiants/11/exercices': { corps: [] } })
    render(<MesExercices etudiantId={11} version={0} />)
    expect(await screen.findByText("Aucun exercice déposé pour l'instant.")).toBeInTheDocument()
  })
})
