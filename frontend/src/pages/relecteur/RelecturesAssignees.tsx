import { useState } from 'react'
import { commencerRelecture, listerRelectures } from '../../api/relectures'
import type { RelectureAssignee } from '../../api/types'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useAction } from '../../hooks/useAction'
import { useApi } from '../../hooks/useApi'
import { libelleStatutRelecteur } from '../../utils/statuts'
import FormulaireRelecture from './FormulaireRelecture'

type Props = {
  etudiantId: number
}

function numero(id: number): string {
  return String(id).padStart(4, '0')
}

type Etat = 'a-commencer' | 'en-cours' | 'rendue'

/** État de la relecture du relecteur connecté : l'autre relecteur de l'exercice n'y change rien (étape 3). */
function etat(r: RelectureAssignee): Etat {
  if (r.rendueAt !== null) return 'rendue'
  return r.lien !== null ? 'en-cours' : 'a-commencer'
}

function statut(r: RelectureAssignee): string {
  const e = etat(r)
  if (e === 'rendue') return `Rendue · ${r.note} / 20`
  return libelleStatutRelecteur(e === 'en-cours' ? 'EN_COURS_DE_RELECTURE' : 'EN_ATTENTE_RELECTURE')
}

/** Spécification 08 (EF6) : relectures assignées, « Commencer la relecture », puis le formulaire. */
export default function RelecturesAssignees({ etudiantId }: Props) {
  const relectures = useApi(`relectures-${etudiantId}`, () => listerRelectures(etudiantId))
  const [ouverte, setOuverte] = useState<RelectureAssignee | null>(null)
  const commencer = useAction(commencerRelecture)

  async function ouvrir(r: RelectureAssignee) {
    const commencee = etat(r) === 'en-cours' ? r : await commencer.executer(r.id, etudiantId)
    if (commencee) {
      setOuverte(commencee)
      relectures.recharger()
    }
  }

  function apresRendu() {
    setOuverte(null)
    relectures.recharger()
  }

  if (relectures.chargement && !relectures.donnees) return <Chargement />
  if (relectures.erreur) return <MessageErreur erreur={relectures.erreur} />
  if (relectures.donnees?.length === 0) return <p>Aucune relecture ne vous est assignée pour l'instant.</p>

  return (
    <>
      <ul className="liste-relectures">
        {relectures.donnees?.map((r) => (
          <li key={r.id} className={ouverte?.id === r.id ? 'relecture active' : 'relecture'}>
            <div>
              <strong>Exercice n° {numero(r.exerciceId)}</strong>
              <span className="chargement"> · {r.sessionTitre}</span>
            </div>
            <span className={etat(r) === 'rendue' ? 'statut-ferme' : 'statut-attente'}>{statut(r)}</span>
            {etat(r) !== 'rendue' ? (
              <button type="button" disabled={commencer.enCours} onClick={() => ouvrir(r)}>
                {etat(r) === 'a-commencer' ? 'Commencer la relecture' : 'Reprendre la relecture'}
              </button>
            ) : null}
          </li>
        ))}
      </ul>
      <MessageErreur erreur={commencer.erreur} />

      {ouverte ? (
        <>
          <h2>
            Exercice n° {numero(ouverte.exerciceId)} · {ouverte.sessionTitre}
          </h2>
          <FormulaireRelecture key={ouverte.id} relecture={ouverte} etudiantId={etudiantId} onRendue={apresRendu} />
        </>
      ) : null}
    </>
  )
}
