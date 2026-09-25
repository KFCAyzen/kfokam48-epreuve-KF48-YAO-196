import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import type { RelectureAssignee } from '../../api/types'
import { simulerApi, type Appel } from '../../test/apiSimulee'
import RelecturesAssignees from './RelecturesAssignees'

const LIEN = 'https://github.com/b-nkoulou/kf48-openapi-atelier'

function relecture(champs: Partial<RelectureAssignee>): RelectureAssignee {
  return { id: 412, sessionId: 6, sessionTitre: 'Conception d’API REST', statut: 'EN_ATTENTE_RELECTURE', lien: null, note: null, commentaire: null, rendueAt: null, ...champs }
}

function afficher(liste: RelectureAssignee[], rendu: (appel: Appel) => { statut: number; corps: unknown }) {
  let etat = liste
  const appels = simulerApi({
    'GET /api/etudiants/11/relectures': () => ({ corps: etat }),
    'POST /api/relectures/412/debut': () => {
      etat = [relecture({ statut: 'EN_COURS_DE_RELECTURE', lien: LIEN })]
      return { corps: etat[0] }
    },
    'POST /api/relectures/412': (appel) => {
      const reponse = rendu(appel)
      if (reponse.statut === 200) etat = [relecture({ statut: 'RELU', lien: LIEN, note: 14 })]
      return reponse
    },
  })
  render(<RelecturesAssignees etudiantId={11} />)
  return { appels, utilisateur: userEvent.setup() }
}

describe('RelecturesAssignees (spécification 08, EF6)', () => {
  it('annonce l’absence de relecture assignée', async () => {
    afficher([], () => ({ statut: 200, corps: {} }))
    expect(await screen.findByText("Aucune relecture ne vous est assignée pour l'instant.")).toBeInTheDocument()
  })

  it('commence la relecture avec l’en-tête X-Etudiant-Id et révèle le lien (RG23)', async () => {
    const { appels, utilisateur } = afficher([relecture({})], () => ({ statut: 200, corps: {} }))
    expect(await screen.findByText('À commencer')).toBeInTheDocument()
    expect(screen.queryByText(LIEN)).not.toBeInTheDocument()

    await utilisateur.click(screen.getByRole('button', { name: 'Commencer la relecture' }))

    const debut = appels.find((a) => a.chemin === '/api/relectures/412/debut')
    expect(debut?.entetes['X-Etudiant-Id']).toBe('11')
    expect(await screen.findByRole('link', { name: LIEN })).toBeInTheDocument()
  })

  it('rend la note et le commentaire, puis la ligne passe « Rendue · 14 / 20 »', async () => {
    const { appels, utilisateur } = afficher([relecture({ statut: 'EN_COURS_DE_RELECTURE', lien: LIEN })], () => ({
      statut: 200,
      corps: { id: 412, statut: 'RELU', note: 14, commentaire: 'Bien', rendueAt: '2026-09-25T11:00:00Z' },
    }))
    await utilisateur.click(await screen.findByRole('button', { name: 'Reprendre la relecture' }))
    await utilisateur.type(screen.getByLabelText(/Note/), '14')
    await utilisateur.type(screen.getByLabelText('Commentaire'), 'Contrat complet.')
    await utilisateur.click(screen.getByRole('button', { name: 'Rendre la relecture' }))

    const rendu = appels.find((a) => a.chemin === '/api/relectures/412')
    expect(rendu?.corps).toEqual({ note: 14, commentaire: 'Contrat complet.' })
    expect(rendu?.entetes['X-Etudiant-Id']).toBe('11')
    expect(await screen.findByText('Rendue · 14 / 20')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Rendre la relecture' })).not.toBeInTheDocument()
  })

  it('affiche le message NOTE_INVALIDE de l’API sous la note et garde la saisie (RG3)', async () => {
    const { utilisateur } = afficher([relecture({ statut: 'EN_COURS_DE_RELECTURE', lien: LIEN })], () => ({
      statut: 400,
      corps: { code: 'NOTE_INVALIDE', message: 'La note doit être un nombre entier compris entre 0 et 20.' },
    }))
    await utilisateur.click(await screen.findByRole('button', { name: 'Reprendre la relecture' }))
    await utilisateur.type(screen.getByLabelText(/Note/), '21')
    await utilisateur.type(screen.getByLabelText('Commentaire'), 'Très bien.')
    await utilisateur.click(screen.getByRole('button', { name: 'Rendre la relecture' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('entre 0 et 20')
    expect(screen.getByLabelText(/Note/)).toHaveValue('21')
  })
})
