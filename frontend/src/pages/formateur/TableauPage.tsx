import ChoixPromotion from '../../components/ChoixPromotion'
import { useMemoire } from '../../hooks/useMemoire'
import OngletsFormateur from './OngletsFormateur'
import TableauPromotion from './TableauPromotion'

/** Écran formateur « Tableau de la promotion » (spécification 03, EF7). */
export default function TableauPage() {
  const [promotionId, setPromotionId] = useMemoire<number | null>('formateur.promotion', null)

  return (
    <>
      <p className="surtitre">Suivi</p>
      <h1>Tableau de la promotion</h1>
      <OngletsFormateur />
      <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
      {promotionId === null ? <p className="chargement">Choisissez une promotion.</p> : <TableauPromotion promotionId={promotionId} />}
    </>
  )
}
