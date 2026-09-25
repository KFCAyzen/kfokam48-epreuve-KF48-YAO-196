import type { SessionResume } from '../../api/types'
import { useMaintenant } from '../../hooks/useMaintenant'
import { heure, minuteur } from '../../utils/format'

type Props = {
  session: SessionResume
  totalEtudiants: number | undefined
}

/**
 * Code de présence à projeter en salle (spécification 01). Le temps restant n'est qu'un affichage
 * de expirationAt - maintenant : c'est l'API qui décide de l'expiration (RG1).
 */
export default function PanneauCode({ session, totalEtudiants }: Props) {
  const maintenant = useMaintenant()
  const debut = Date.parse(session.ouvertureAt)
  const fin = Date.parse(session.expirationAt)
  const restant = fin - maintenant
  const expire = restant <= 0
  const progression = Math.min(100, Math.max(0, (restant / (fin - debut)) * 100))

  return (
    <section className="panneau" aria-labelledby="titre-code">
      <div className="panneau-entete">
        <p id="titre-code" className="surtitre">
          Code de présence<sup>1</sup> · {session.titre}
        </p>
        <p className={expire ? 'statut-ferme' : 'statut-ouvert'}>{expire ? 'Code expiré' : '● Session ouverte'}</p>
      </div>

      <p className={expire ? 'code-cases barre' : 'code-cases'} aria-label={`Code ${session.code.split('').join(' ')}`}>
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
        {expire ? (
          <span>
            Ouverte à <strong>{heure(session.ouvertureAt)}</strong> · <strong>Code expiré à {heure(session.expirationAt)}</strong>
          </span>
        ) : (
          <>
            <span>
              Ouverte à <strong>{heure(session.ouvertureAt)}</strong> · expire à{' '}
              <strong>{heure(session.expirationAt)}</strong>
              <sup>2</sup>
            </span>
            <span className="mono">{minuteur(restant)} restantes</span>
          </>
        )}
      </p>

      <p>
        <strong>{session.presents}</strong> présence{session.presents > 1 ? 's' : ''} enregistrée
        {session.presents > 1 ? 's' : ''}
        {totalEtudiants !== undefined ? ` sur ${totalEtudiants}` : ''}
      </p>
    </section>
  )
}
