import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { simulerApi, type Reponse } from '../../test/apiSimulee'
import MarquerPresence from './MarquerPresence'

function erreur(statut: number, code: string, message: string): Reponse {
  return { statut, corps: { code, message } }
}

async function saisirEtValider(code: string) {
  const utilisateur = userEvent.setup()
  render(<MarquerPresence etudiantId={11} />)
  await utilisateur.type(screen.getByLabelText(/Saisissez le code/), code)
  await utilisateur.click(screen.getByRole('button', { name: 'Valider ma présence' }))
}

describe('MarquerPresence (spécification 06, EF1)', () => {
  it('passe la saisie en majuscules et envoie le code avec l’identifiant de l’étudiant', async () => {
    const appels = simulerApi({
      'POST /api/presences': { statut: 201, corps: { id: 1, sessionId: 6, etudiantId: 11, source: 'ETUDIANT' } },
    })
    await saisirEtValider('k7m4qx')

    expect(appels[0].corps).toEqual({ code: 'K7M4QX', etudiantId: 11 })
    expect(await screen.findByText('Présence enregistrée.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Valider ma présence' })).not.toBeInTheDocument()
  })

  it('limite la saisie à six caractères', async () => {
    const utilisateur = userEvent.setup()
    render(<MarquerPresence etudiantId={11} />)
    const champ = screen.getByLabelText(/Saisissez le code/)
    await utilisateur.type(champ, 'ABCDEFGH')
    expect(champ).toHaveValue('ABCDEF')
  })

  it('code inconnu : affiche le message de l’API et garde le code pour correction (RG6)', async () => {
    simulerApi({ 'POST /api/presences': erreur(400, 'CODE_INCONNU', 'Code inconnu. Vérifiez le code affiché par votre formateur.') })
    await saisirEtValider('K7M4QZ')

    expect(await screen.findByRole('alert')).toHaveTextContent('Code inconnu.')
    expect(screen.getByLabelText(/Saisissez le code/)).toHaveValue('K7M4QZ')
  })

  it('code expiré : affiche le message de l’API (RG1)', async () => {
    simulerApi({ 'POST /api/presences': erreur(410, 'CODE_EXPIRE', 'Le code de présence a expiré.') })
    await saisirEtValider('K7M4QX')
    expect(await screen.findByRole('alert')).toHaveTextContent('Le code de présence a expiré.')
  })

  it('déjà présent : information, et le formulaire disparaît (RG4)', async () => {
    simulerApi({ 'POST /api/presences': erreur(409, 'DEJA_PRESENT', 'Votre présence est déjà enregistrée pour cette session.') })
    await saisirEtValider('K7M4QX')

    expect(await screen.findByRole('status')).toHaveTextContent('Votre présence est déjà enregistrée')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Valider ma présence' })).not.toBeInTheDocument()
  })
})
