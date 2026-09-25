import type { SessionResume } from '../../api/types'
import { useMaintenant } from '../../hooks/useMaintenant'
import { heure, minuteur } from '../../utils/format'

type Props = {
  session: SessionResume
  totalEtudiants: number | undefined
}

/** Code de présence à projeter en salle, avec ses heures d'ouverture et d'expiration (maquette 01, RG1). */
export default function PanneauCode({ session, totalEtudiants }: Props) {
  const maintenant = useMaintenant()
  const debut = Date.parse(session.ouvertureAt)
  const fin = Date.parse(session.expirationAt)
  const restant = fin - maintenant
  const ouverte = session.clotureeAt === null && restant > 0
  const progression = Math.min(100, Math.max(0, (restant / (fin - debut)) * 100))

  return (
    <section className="panneau" aria-labelledby="titre-code">
      <div className="panneau-entete">
        <p id="titre-code" className="surtitre">
          Code de présence<sup>1</sup>
        </p>
        <p className={ouverte ? 'statut-ouvert' : 'statut-ferme'}>
          {session.clotureeAt ? 'Session clôturée' : ouverte ? '● Session ouverte' : 'Code expiré'}
        </p>
      </div>

      <p className="code-cases" aria-label={`Code ${session.code.split('').join(' ')}`}>
        {session.code.split('').map((caractere, position) => (
          <span key={position} aria-hidden="true">
            {caractere}
          </span>
        ))}
      </p>

      <div className="jauge" aria-hidden="true">
        <div style={{ width: `${progression}%` }} />
      </div>
      <p className="ligne-horaire">
        <span>
          Ouverte à <strong>{heure(session.ouvertureAt)}</strong> · expire à <strong>{heure(session.expirationAt)}</strong>
          <sup>2</sup>
        </span>
        <span className="mono" aria-live="off">
          {ouverte ? `${minuteur(restant)} restantes` : 'Code expiré'}
        </span>
      </p>

      <p>
        <strong>{session.presents}</strong> présence{session.presents > 1 ? 's' : ''} enregistrée
        {session.presents > 1 ? 's' : ''}
        {totalEtudiants !== undefined && ` sur ${totalEtudiants}`}
      </p>
    </section>
  )
}
