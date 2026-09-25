import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { simulerApi } from '../test/apiSimulee'
import AvecIdentite from './AvecIdentite'
import IdentiteProvider from './IdentiteProvider'

const PROMOTIONS = [{ id: 1, nom: '2026-A · Développement logiciel' }]
const ETUDIANTS = [
  { id: 10, nom: 'Ateba, Grâce', promotionId: 1 },
  { id: 11, nom: 'Mbarga, Aïcha', promotionId: 1 },
]

function afficher() {
  return render(
    <IdentiteProvider>
      <AvecIdentite>{(identite) => <p>Connecté : {identite.nom}</p>}</AvecIdentite>
    </IdentiteProvider>,
  )
}

describe('Identification (spécification 05, EF3)', () => {
  it('demande la promotion puis le nom, sans mot de passe, et conserve le choix', async () => {
    simulerApi({
      'GET /api/promotions': { corps: PROMOTIONS },
      'GET /api/promotions/1/etudiants': { corps: ETUDIANTS },
    })
    const utilisateur = userEvent.setup()
    afficher()

    expect(screen.getByRole('heading', { name: 'Qui êtes-vous ?' })).toBeInTheDocument()
    expect(screen.queryByLabelText(/mot de passe/i)).not.toBeInTheDocument()

    await utilisateur.selectOptions(await screen.findByLabelText('Promotion'), '1')
    const continuer = screen.getByRole('button', { name: 'Continuer' })
    expect(continuer).toBeDisabled()

    await utilisateur.click(await screen.findByRole('radio', { name: 'Mbarga, Aïcha' }))
    expect(continuer).toBeEnabled()
    await utilisateur.click(continuer)

    expect(screen.getByText('Connecté : Mbarga, Aïcha')).toBeInTheDocument()
    expect(JSON.parse(window.localStorage.getItem('kf48.v1.identite') ?? '{}')).toMatchObject({
      etudiantId: 11,
      promotionId: 1,
    })
  })

  it('saute l’identification quand une identité est déjà conservée', () => {
    window.localStorage.setItem(
      'kf48.v1.identite',
      JSON.stringify({ etudiantId: 10, nom: 'Ateba, Grâce', promotionId: 1, promotionNom: '2026-A' }),
    )
    afficher()
    expect(screen.getByText('Connecté : Ateba, Grâce')).toBeInTheDocument()
  })
})
