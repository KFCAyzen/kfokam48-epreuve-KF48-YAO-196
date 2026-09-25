import { render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { simulerApi } from '../../test/apiSimulee'
import TableauPromotion from './TableauPromotion'

const SESSION = { titre: 'S', promotionId: 1, code: 'ABCDEF', ouvertureAt: '2026-09-25T10:15:00Z', expirationAt: '2026-09-25T10:30:00Z', clotureeAt: null, presents: 0, exercicesDeposes: 0, exercicesEnAttente: 0 }

const LIGNES = [
  { etudiantId: 1, nom: 'Mbarga, Aïcha', presences: 6, exercicesDeposes: 5, moyenne: 14.75, relecturesEnAttente: 2, moyenneProvisoire: true },
  { etudiantId: 2, nom: 'Tagne, Joël', presences: 1, exercicesDeposes: 1, moyenne: null, relecturesEnAttente: 0, moyenneProvisoire: false },
]

describe('TableauPromotion (spécification 03, EF7)', () => {
  it('affiche les valeurs de l’API sans les recalculer, et « — » quand la moyenne est null (RG19)', async () => {
    simulerApi({
      'GET /api/tableau?promotionId=1': { corps: LIGNES },
      'GET /api/promotions/1/sessions': { corps: Array.from({ length: 6 }, (_, i) => ({ ...SESSION, id: i + 1 })) },
    })
    render(<TableauPromotion promotionId={1} />)

    const aicha = (await screen.findByText('Mbarga, Aïcha')).closest('tr')!
    expect(within(aicha).getByText('6 / 6')).toBeInTheDocument()
    expect(within(aicha).getByText('14,75')).toBeInTheDocument()
    expect(aicha).toHaveClass('a-signaler')
    expect(within(aicha).getByText('provisoire')).toBeInTheDocument()

    const joel = screen.getByText('Tagne, Joël').closest('tr')!
    expect(within(joel).getByText('—')).toBeInTheDocument()
    expect(joel).not.toHaveClass('a-signaler')
    expect(within(joel).queryByText('provisoire')).not.toBeInTheDocument()
  })

  it('n’affiche pas de ligne de total « Promotion » (spécification 03)', async () => {
    simulerApi({ 'GET /api/tableau?promotionId=1': { corps: LIGNES }, 'GET /api/promotions/1/sessions': { corps: [] } })
    render(<TableauPromotion promotionId={1} />)
    await screen.findByText('Mbarga, Aïcha')
    expect(screen.getAllByRole('row')).toHaveLength(3)
  })

  it('affiche le message de l’API quand la promotion n’existe pas', async () => {
    simulerApi({
      'GET /api/tableau?promotionId=1': { statut: 404, corps: { code: 'PROMOTION_INCONNUE', message: "Cette promotion n'existe pas." } },
      'GET /api/promotions/1/sessions': { statut: 404, corps: { code: 'PROMOTION_INCONNUE', message: "Cette promotion n'existe pas." } },
    })
    render(<TableauPromotion promotionId={1} />)
    expect(await screen.findByRole('alert')).toHaveTextContent("Cette promotion n'existe pas.")
  })
})
