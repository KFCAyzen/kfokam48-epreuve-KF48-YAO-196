import { listerEtudiants } from '../../api/promotions'
import { listerSessions } from '../../api/sessions'
import ChoixPromotion from '../../components/ChoixPromotion'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useApi } from '../../hooks/useApi'
import { useMemoire } from '../../hooks/useMemoire'
import OuvrirSession from './OuvrirSession'
import PanneauCode from './PanneauCode'
import SessionsPrecedentes from './SessionsPrecedentes'

/** Écran formateur (F2) : ouvrir une session (maquette 01). */
export default function FormateurPage() {
  const [promotionId, setPromotionId] = useMemoire<number | null>('formateur.promotion', null)

  const sessions = useApi(promotionId === null ? null : `sessions-${promotionId}`, () => listerSessions(promotionId!))
  const etudiants = useApi(promotionId === null ? null : `etudiants-${promotionId}`, () =>
    listerEtudiants(promotionId!),
  )
  const totalEtudiants = etudiants.donnees?.length
  const derniere = sessions.donnees?.[0]

  return (
    <>
      <p className="surtitre">Sessions de cours</p>
      <h1>Ouvrir une session</h1>

      <div className="grille-2">
        <div>
          <ChoixPromotion valeur={promotionId} onChange={setPromotionId} />
          <OuvrirSession promotionId={promotionId} onOuverte={sessions.recharger} />
        </div>
        <div>
          {derniere ? (
            <PanneauCode session={derniere} totalEtudiants={totalEtudiants} />
          ) : (
            <p className="chargement">
              {promotionId === null ? 'Choisissez une promotion.' : 'Aucune session ouverte pour le moment.'}
            </p>
          )}
        </div>
      </div>

      {sessions.chargement && <Chargement />}
      <MessageErreur erreur={sessions.erreur} />
      {sessions.donnees && <SessionsPrecedentes sessions={sessions.donnees} totalEtudiants={totalEtudiants} />}
    </>
  )
}
