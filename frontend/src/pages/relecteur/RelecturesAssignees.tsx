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

function statut(r: RelectureAssignee): string {
  return r.statut === 'RELU' ? `Rendue · ${r.note} / 20` : libelleStatutRelecteur(r.statut)
}

/** Spécification 08 (EF6) : relectures assignées, « Commencer la relecture », puis le formulaire. */
export default function RelecturesAssignees({ etudiantId }: Props) {
  const relectures = useApi(`relectures-${etudiantId}`, () => listerRelectures(etudiantId))
  const [ouverte, setOuverte] = useState<RelectureAssignee | null>(null)
  const commencer = useAction(commencerRelecture)

  async function ouvrir(r: RelectureAssignee) {
    const commencee = r.statut === 'EN_COURS_DE_RELECTURE' && r.lien ? r : await commencer.executer(r.id, etudiantId)
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
              <strong>Exercice n° {numero(r.id)}</strong>
              <span className="chargement"> · {r.sessionTitre}</span>
            </div>
            <span className={r.statut === 'RELU' ? 'statut-ferme' : 'statut-attente'}>{statut(r)}</span>
            {r.statut === 'EN_ATTENTE_RELECTURE' || r.statut === 'EN_COURS_DE_RELECTURE' ? (
              <button type="button" disabled={commencer.enCours} onClick={() => ouvrir(r)}>
                {r.statut === 'EN_ATTENTE_RELECTURE' ? 'Commencer la relecture' : 'Reprendre la relecture'}
              </button>
            ) : null}
          </li>
        ))}
      </ul>
      <MessageErreur erreur={commencer.erreur} />

      {ouverte ? (
        <>
          <h2>
            Exercice n° {numero(ouverte.id)} · {ouverte.sessionTitre}
          </h2>
          <FormulaireRelecture key={ouverte.id} relecture={ouverte} etudiantId={etudiantId} onRendue={apresRendu} />
        </>
      ) : null}
    </>
  )
}
