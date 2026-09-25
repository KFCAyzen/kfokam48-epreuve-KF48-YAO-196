import { listerMesExercices } from '../../api/exercices'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useApi } from '../../hooks/useApi'
import { moyenne } from '../../utils/format'
import { libelleStatut } from '../../utils/statuts'

type Props = {
  etudiantId: number
  /** Change quand un exercice vient d'être déposé : la liste est rechargée. */
  version: number
}

/**
 * Spécification 07, « Mes exercices » (EF12) : statut, lien, note retenue sur 20, « provisoire » tant
 * qu'un seul des deux relecteurs a rendu (RG25), commentaires signés « Relecteur anonyme » (RG20).
 * La note vient de l'API, le front ne la calcule pas (F3).
 */
export default function MesExercices({ etudiantId, version }: Props) {
  const exercices = useApi(`mes-exercices-${etudiantId}-${version}`, () => listerMesExercices(etudiantId))

  return (
    <section aria-labelledby="titre-mes-exercices">
      <h2 id="titre-mes-exercices">Mes exercices</h2>
      {exercices.chargement && !exercices.donnees ? <Chargement /> : null}
      <MessageErreur erreur={exercices.erreur} />
      {exercices.donnees?.length === 0 ? <p>Aucun exercice déposé pour l'instant.</p> : null}
      <ul className="liste-exercices">
        {exercices.donnees?.map((e) => (
          <li key={e.id} className="panneau carte-exercice">
            <p className="surtitre">{e.sessionTitre}</p>
            <p>
              <span className={e.statut === 'RELU' ? 'statut-ferme' : 'statut-attente'}>{libelleStatut(e.statut)}</span>
              {' · '}
              <a href={e.lien} target="_blank" rel="noreferrer">
                {e.lien}
              </a>
            </p>
            {e.note !== null ? (
              <p className="note-retenue">
                <strong>{moyenne(e.note)}</strong> sur 20
                {e.noteProvisoire ? <span className="provisoire"> provisoire</span> : null}
              </p>
            ) : null}
            {e.commentaires.map((commentaire, index) => (
              <blockquote key={index}>
                « {commentaire} » <cite>Relecteur anonyme</cite>
              </blockquote>
            ))}
          </li>
        ))}
      </ul>
    </section>
  )
}
