import { lireTableau } from '../../api/tableau'
import { listerSessions } from '../../api/sessions'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useApi } from '../../hooks/useApi'
import { moyenne } from '../../utils/format'

type Props = {
  promotionId: number
}

/**
 * Spécification 03 (EF7) : une ligne par étudiant. Toutes les valeurs viennent de GET /api/tableau,
 * l'écran n'en calcule aucune (F3) ; le dénominateur des présences est le nombre de sessions.
 */
export default function TableauPromotion({ promotionId }: Props) {
  const tableau = useApi(`tableau-${promotionId}`, () => lireTableau(promotionId))
  const sessions = useApi(`sessions-${promotionId}`, () => listerSessions(promotionId))
  const nombreSessions = sessions.donnees?.length

  if (tableau.chargement && !tableau.donnees) return <Chargement />
  if (tableau.erreur) return <MessageErreur erreur={tableau.erreur} />
  if (tableau.donnees?.length === 0) return <p>Aucun étudiant dans cette promotion.</p>

  return (
    <div className="tableau-defilant">
      <table>
        <caption className="legende">Tableau 2. Indicateurs par étudiant</caption>
        <thead>
          <tr>
            <th scope="col">N°</th>
            <th scope="col">Étudiant</th>
            <th scope="col" className="nombre">
              Présences
            </th>
            <th scope="col" className="nombre">
              Exercices déposés
            </th>
            <th scope="col" className="nombre">
              Moyenne / 20<sup>a</sup>
            </th>
            <th scope="col" className="nombre">
              Relectures en attente
            </th>
          </tr>
        </thead>
        <tbody>
          {tableau.donnees?.map((ligne, index) => (
            <tr key={ligne.etudiantId} className={ligne.relecturesEnAttente > 0 ? 'a-signaler' : undefined}>
              <td className="mono">{String(index + 1).padStart(2, '0')}</td>
              <td>{ligne.nom}</td>
              <td className="nombre">
                {ligne.presences}
                {nombreSessions !== undefined ? ` / ${nombreSessions}` : ''}
              </td>
              <td className="nombre">{ligne.exercicesDeposes}</td>
              <td className="nombre">
                {moyenne(ligne.moyenne)}
                {ligne.moyenneProvisoire ? <span className="provisoire"> provisoire</span> : null}
              </td>
              <td className="nombre">
                {ligne.relecturesEnAttente > 0 ? <strong className="statut-attente">{ligne.relecturesEnAttente}</strong> : 0}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="notes">
        <p>
          <sup>a</sup> Moyenne arithmétique des notes reçues sur les exercices relus, arrondie à deux décimales. « — » :
          aucune note reçue. « provisoire » : une des notes retenues ne vient encore que d'un des deux relecteurs.
        </p>
        <p>Présences toutes sources confondues, par l'étudiant ou ajoutées par le formateur.</p>
      </div>
    </div>
  )
}
