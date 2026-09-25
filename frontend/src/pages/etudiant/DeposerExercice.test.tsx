import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { simulerApi } from '../../test/apiSimulee'
import DeposerExercice from './DeposerExercice'

const SESSIONS = [
  { id: 6, titre: 'Conception d’API REST', promotionId: 1, code: 'K7M4QX', ouvertureAt: '2026-09-25T10:15:00Z', expirationAt: '2026-09-25T10:30:00Z', clotureeAt: null, presents: 9, exercicesDeposes: 3, exercicesEnAttente: 3 },
  { id: 5, titre: 'Persistance avec JPA', promotionId: 1, code: 'P5JPA2', ouvertureAt: '2026-09-23T10:15:00Z', expirationAt: '2026-09-23T10:30:00Z', clotureeAt: '2026-09-23T18:00:00Z', presents: 10, exercicesDeposes: 9, exercicesEnAttente: 0 },
]

async function preparer(reponseDepot: { statut: number; corps: unknown }) {
  const appels = simulerApi({
    'GET /api/promotions/1/sessions': { corps: SESSIONS },
    'POST /api/exercices': reponseDepot,
  })
  const utilisateur = userEvent.setup()
  render(<DeposerExercice etudiantId={11} promotionId={1} />)
  return { appels, utilisateur }
}

describe('DeposerExercice (spécification 07, EF4)', () => {
  it('ne propose que les sessions non clôturées de la promotion', async () => {
    await preparer({ statut: 201, corps: { id: 1, statut: 'DEPOSE' } })
    expect(await screen.findByRole('option', { name: 'Conception d’API REST' })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: 'Persistance avec JPA' })).not.toBeInTheDocument()
  })

  it('envoie session, étudiant et lien, puis affiche le statut renvoyé par l’API', async () => {
    const { appels, utilisateur } = await preparer({ statut: 201, corps: { id: 12, statut: 'EN_ATTENTE_RELECTURE' } })
    await utilisateur.selectOptions(await screen.findByLabelText('Session'), '6')
    await utilisateur.type(screen.getByLabelText(/Lien vers l'exercice/), 'https://github.com/ambarga/kf48')
    await utilisateur.click(screen.getByRole('button', { name: 'Déposer' }))

    expect(appels.find((a) => a.methode === 'POST')?.corps).toEqual({
      sessionId: 6,
      etudiantId: 11,
      lien: 'https://github.com/ambarga/kf48',
    })
    expect(await screen.findByRole('status')).toHaveTextContent('en attente de relecture')
  })

  it('affiche le message de l’API et garde le lien saisi en cas de refus (RG10)', async () => {
    const { utilisateur } = await preparer({
      statut: 409,
      corps: { code: 'EXERCICE_DEJA_DEPOSE', message: 'Vous avez déjà déposé un exercice pour cette session.' },
    })
    await utilisateur.selectOptions(await screen.findByLabelText('Session'), '6')
    await utilisateur.type(screen.getByLabelText(/Lien vers l'exercice/), 'https://github.com/ambarga/kf48')
    await utilisateur.click(screen.getByRole('button', { name: 'Déposer' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('déjà déposé')
    expect(screen.getByLabelText(/Lien vers l'exercice/)).toHaveValue('https://github.com/ambarga/kf48')
  })
})
