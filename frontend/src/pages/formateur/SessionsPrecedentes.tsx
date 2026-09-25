import type { SessionResume } from '../../api/types'
import { date } from '../../utils/format'

type Props = {
  sessions: SessionResume[]
  totalEtudiants: number | undefined
}

/** Tableau 1 de la maquette 01 : les sessions de la promotion, la plus récente d'abord. */
export default function SessionsPrecedentes({ sessions, totalEtudiants }: Props) {
  if (sessions.length === 0) {
    return <p className="chargement">Aucune session ouverte pour cette promotion.</p>
  }

  return (
    <div className="tableau-defilant">
      <table>
        <caption className="legende">Tableau 1. Sessions de la promotion</caption>
        <thead>
          <tr>
            <th scope="col">N°</th>
            <th scope="col">Séance</th>
            <th scope="col">Date</th>
            <th scope="col" className="nombre">
              Présents
            </th>
            <th scope="col">Statut</th>
          </tr>
        </thead>
        <tbody>
          {sessions.map((s, index) => (
            <tr key={s.id}>
              <td className="mono">S{sessions.length - index}</td>
              <td>{s.titre}</td>
              <td>{date(s.ouvertureAt)}</td>
              <td className="nombre">
                {s.presents}
                {totalEtudiants !== undefined && ` / ${totalEtudiants}`}
              </td>
              <td className={s.clotureeAt ? 'statut-ferme' : 'statut-ouvert'}>{s.clotureeAt ? 'Clôturée' : 'Ouverte'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
