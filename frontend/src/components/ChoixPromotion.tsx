import { useId } from 'react'
import { listerPromotions } from '../api/promotions'
import { useApi } from '../hooks/useApi'
import { MessageErreur } from './Etat'

type Props = {
  valeur: number | null
  onChange: (promotionId: number | null) => void
  libelle?: string
}

/** Liste déroulante des promotions, chargée depuis l'API. */
export default function ChoixPromotion({ valeur, onChange, libelle = 'Promotion' }: Props) {
  const id = useId()
  const { donnees: promotions, chargement, erreur } = useApi('promotions', listerPromotions)

  return (
    <>
      <label htmlFor={id}>{libelle}</label>
      <select
        id={id}
        value={valeur ?? ''}
        disabled={chargement}
        onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
      >
        <option value="">{chargement ? 'Chargement…' : 'Choisir une promotion'}</option>
        {promotions?.map((p) => (
          <option key={p.id} value={p.id}>
            {p.nom}
          </option>
        ))}
      </select>
      <MessageErreur erreur={erreur} />
    </>
  )
}
