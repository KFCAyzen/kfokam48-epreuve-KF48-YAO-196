import { useId } from 'react'
import { listerPromotions } from '../api/promotions'
import type { Promotion } from '../api/types'
import { useApi } from '../hooks/useApi'
import { MessageErreur } from './Etat'

type Props = {
  valeur: number | null
  /** Reçoit l'identifiant choisi et la promotion complète (null si aucune). */
  onChange: (promotionId: number | null, promotion: Promotion | null) => void
  libelle?: string
}

/** Liste déroulante des promotions, chargée depuis l'API. */
export default function ChoixPromotion({ valeur, onChange, libelle = 'Promotion' }: Props) {
  const id = useId()
  const { donnees: promotions, chargement, erreur } = useApi('promotions', listerPromotions)

  function changer(brut: string) {
    const promotion = promotions?.find((p) => p.id === Number(brut)) ?? null
    onChange(promotion?.id ?? null, promotion)
  }

  return (
    <>
      <label htmlFor={id}>{libelle}</label>
      <select id={id} value={valeur ?? ''} disabled={chargement} onChange={(e) => changer(e.target.value)}>
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
