import { useState } from 'react'
import { listerEtudiants } from '../../api/promotions'
import { listerSessions } from '../../api/sessions'
import ChoixPromotion from '../../components/ChoixPromotion'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useApi } from '../../hooks/useApi'
import { useMemoire } from '../../hooks/useMemoire'
import OuvrirSession from './OuvrirSession'
import PanneauCode from './PanneauCode'
import SessionsPrecedentes from './SessionsPrecedentes'

/** Écran formateur (F2) : ouvrir une session et projeter son code (spécification 01). */
export default function FormateurPage() {
  const [promotionId, setPromotionId] = useMemoire<number | null>('formateur.promotion', null)
  const [sessionAffichee, setSessionAffichee] = useState<number | null>(null)

  const sessions = useApi(promotionId === null ? null : `sessions-${promotionId}`, () => listerSessions(promotionId!))
  const etudiants = useApi(promotionId === null ? null : `etudiants-${promotionId}`, () =>
    listerEtudiants(promotionId!),
  )
  const totalEtudiants = etudiants.donnees?.length
  const affichee = sessions.donnees?.find((s) => s.id === sessionAffichee)

  function changerDePromotion(id: number | null) {
    setPromotionId(id)
    setSessionAffichee(null)
  }

  return (
    <>
      <p className="surtitre">Sessions de cours</p>
      <h1>Ouvrir une session</h1>

      <ChoixPromotion valeur={promotionId} onChange={changerDePromotion} />

      {affichee ? (
        <>
          <PanneauCode session={affichee} totalEtudiants={totalEtudiants} />
          <button type="button" className="secondaire" onClick={() => setSessionAffichee(null)}>
            Ouvrir une autre session
          </button>
        </>
      ) : (
        <OuvrirSession
          promotionId={promotionId}
          onOuverte={(session) => {
            setSessionAffichee(session.id)
            sessions.recharger()
          }}
        />
      )}

      {sessions.chargement ? <Chargement /> : null}
      <MessageErreur erreur={sessions.erreur} />
      {sessions.donnees ? <SessionsPrecedentes sessions={sessions.donnees} totalEtudiants={totalEtudiants} /> : null}
    </>
  )
}
