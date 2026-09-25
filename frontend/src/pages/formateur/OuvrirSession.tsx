import { useId, useState, type FormEvent } from 'react'
import { ouvrirSession } from '../../api/sessions'
import type { SessionOuverte } from '../../api/types'
import { MessageErreur } from '../../components/Etat'
import { useAction } from '../../hooks/useAction'

type Props = {
  promotionId: number | null
  onOuverte: (session: SessionOuverte) => void
}

/** Formulaire de la spécification 01 : titre de la séance puis « Ouvrir la session » (EF2). */
export default function OuvrirSession({ promotionId, onOuverte }: Props) {
  const idTitre = useId()
  const [titre, setTitre] = useState('')
  const { executer, enCours, erreur } = useAction(ouvrirSession)
  const complet = titre.trim() !== '' && promotionId !== null

  async function soumettre(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    if (promotionId === null) return
    const session = await executer(titre, promotionId)
    if (session) {
      setTitre('')
      onOuverte(session)
    }
  }

  return (
    <form onSubmit={soumettre}>
      <label htmlFor={idTitre}>Titre de la séance</label>
      <input
        id={idTitre}
        value={titre}
        onChange={(e) => setTitre(e.target.value)}
        placeholder="Conception d'API REST — atelier contrat OpenAPI"
      />
      <MessageErreur erreur={erreur} />
      <button type="submit" disabled={enCours || !complet}>
        {enCours ? 'Ouverture…' : 'Ouvrir la session'}
      </button>
      <div className="notes">
        <p>
          <sup>1</sup> Le code comporte six caractères, sans 0, O, 1 ni I, et n'est jamais réutilisé.
        </p>
        <p>
          <sup>2</sup> Il expire quinze minutes après l'ouverture. Vous pourrez ensuite ajouter une présence à la main
          jusqu'à la clôture.
        </p>
      </div>
    </form>
  )
}
