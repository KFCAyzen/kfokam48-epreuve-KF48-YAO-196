import { useId, useState, type FormEvent } from 'react'
import { deposerExercice } from '../../api/exercices'
import { listerSessions } from '../../api/sessions'
import type { ExerciceDepose } from '../../api/types'
import { Chargement, MessageErreur } from '../../components/Etat'
import { useAction } from '../../hooks/useAction'
import { useApi } from '../../hooks/useApi'
import { libelleStatut } from '../../utils/statuts'

type Props = {
  etudiantId: number
  promotionId: number
}

/**
 * Spécification 07, zone « Dépôt » (EF4) : une session non clôturée de sa promotion et le lien.
 * La forme du lien, l'unicité et la clôture sont vérifiées par l'API (RG10, RG11, RG12).
 */
export default function DeposerExercice({ etudiantId, promotionId }: Props) {
  const idSession = useId()
  const idLien = useId()
  const [sessionId, setSessionId] = useState<number | null>(null)
  const [lien, setLien] = useState('')
  const [depose, setDepose] = useState<ExerciceDepose | null>(null)
  const sessions = useApi(`sessions-${promotionId}`, () => listerSessions(promotionId))
  const { executer, enCours, erreur } = useAction(deposerExercice)

  const ouvertes = sessions.donnees?.filter((s) => s.clotureeAt === null) ?? []

  async function soumettre(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    if (sessionId === null) return
    const exercice = await executer(sessionId, etudiantId, lien.trim())
    if (exercice) {
      setDepose(exercice)
      setLien('')
    }
  }

  return (
    <section aria-labelledby="titre-depot">
      <h2 id="titre-depot">Déposer un exercice</h2>
      {sessions.chargement ? <Chargement /> : null}
      <MessageErreur erreur={sessions.erreur} />

      <form onSubmit={soumettre}>
        <label htmlFor={idSession}>Session</label>
        <select
          id={idSession}
          value={sessionId ?? ''}
          onChange={(e) => {
            setSessionId(e.target.value ? Number(e.target.value) : null)
            setDepose(null)
          }}
        >
          <option value="">{ouvertes.length ? 'Choisir une session' : 'Aucune session ouverte'}</option>
          {ouvertes.map((s) => (
            <option key={s.id} value={s.id}>
              {s.titre}
            </option>
          ))}
        </select>

        <label htmlFor={idLien}>
          Lien vers l'exercice<sup>1</sup>
        </label>
        <input
          id={idLien}
          type="url"
          inputMode="url"
          value={lien}
          onChange={(e) => setLien(e.target.value)}
          placeholder="https://"
        />

        <MessageErreur erreur={erreur} />
        {depose ? (
          <p className="succes" role="status">
            Exercice déposé : {libelleStatut(depose.statut).toLowerCase()}.
          </p>
        ) : null}

        <button type="submit" disabled={enCours || sessionId === null || lien.trim() === ''}>
          {enCours ? 'Dépôt…' : 'Déposer'}
        </button>

        <div className="notes">
          <p>
            <sup>1</sup> Adresse commençant par http:// ou https://, 500 caractères au plus. Un seul exercice par session.
          </p>
          <p>
            <sup>2</sup> Le lien peut être remplacé tant que votre relecteur n'a pas commencé la relecture.
          </p>
          <p>
            <sup>3</sup> L'identité du relecteur n'est jamais communiquée à l'auteur.
          </p>
        </div>
      </form>
    </section>
  )
}
