import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { simulerApi } from '../../test/apiSimulee'
import OuvrirSession from './OuvrirSession'

const SESSION = { id: 7, code: 'K7M4QX', ouvertureAt: '2026-09-25T10:15:00Z', expirationAt: '2026-09-25T10:30:00Z' }

describe('OuvrirSession (spécification 01, EF2)', () => {
  it('n’active le bouton qu’une fois le titre saisi et la promotion choisie', async () => {
    const utilisateur = userEvent.setup()
    const { rerender } = render(<OuvrirSession promotionId={null} onOuverte={vi.fn()} />)
    const bouton = screen.getByRole('button', { name: 'Ouvrir la session' })

    await utilisateur.type(screen.getByLabelText('Titre de la séance'), 'Streams Java')
    expect(bouton).toBeDisabled()

    rerender(<OuvrirSession promotionId={1} onOuverte={vi.fn()} />)
    expect(bouton).toBeEnabled()
  })

  it('envoie POST /api/sessions et transmet la session ouverte', async () => {
    const appels = simulerApi({ 'POST /api/sessions': { statut: 201, corps: SESSION } })
    const onOuverte = vi.fn()
    const utilisateur = userEvent.setup()
    render(<OuvrirSession promotionId={1} onOuverte={onOuverte} />)

    await utilisateur.type(screen.getByLabelText('Titre de la séance'), 'Streams Java')
    await utilisateur.click(screen.getByRole('button', { name: 'Ouvrir la session' }))

    expect(appels[0].corps).toEqual({ titre: 'Streams Java', promotionId: 1 })
    expect(onOuverte).toHaveBeenCalledWith(SESSION)
  })

  it('affiche le message de l’API et conserve la saisie en cas d’erreur (ENF8)', async () => {
    simulerApi({
      'POST /api/sessions': { statut: 400, corps: { code: 'PROMOTION_INCONNUE', message: "Cette promotion n'existe pas." } },
    })
    const onOuverte = vi.fn()
    const utilisateur = userEvent.setup()
    render(<OuvrirSession promotionId={99} onOuverte={onOuverte} />)

    await utilisateur.type(screen.getByLabelText('Titre de la séance'), 'Streams Java')
    await utilisateur.click(screen.getByRole('button', { name: 'Ouvrir la session' }))

    expect(await screen.findByRole('alert')).toHaveTextContent("Cette promotion n'existe pas.")
    expect(screen.getByLabelText('Titre de la séance')).toHaveValue('Streams Java')
    expect(onOuverte).not.toHaveBeenCalled()
  })
})
