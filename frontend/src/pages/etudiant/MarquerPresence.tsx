import { useId, useState, type FormEvent } from 'react'
import { marquerPresence } from '../../api/presences'
import type { Presence } from '../../api/types'
import { MessageErreur } from '../../components/Etat'
import { useAction } from '../../hooks/useAction'

type Props = {
  etudiantId: number
  onEnregistree?: (presence: Presence) => void
}

const LONGUEUR_CODE = 6

/**
 * Spécification 06 (EF1) : l'étudiant saisit le code affiché en salle. Le front ne compte pas les
 * essais et ne valide pas l'alphabet : c'est l'API qui dit si le code existe, a expiré ou bloque (F3).
 */
export default function MarquerPresence({ etudiantId, onEnregistree }: Props) {
  const idCode = useId()
  const [code, setCode] = useState('')
  const [issue, setIssue] = useState<'enregistree' | 'deja-presente' | null>(null)
  const { executer, enCours, erreur } = useAction(marquerPresence)

  async function soumettre(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    const presence = await executer(code, etudiantId)
    if (presence) {
      setIssue('enregistree')
      onEnregistree?.(presence)
    }
  }

  // 409 DEJA_PRESENT est une information, pas une erreur : le formulaire disparaît (RG4).
  if (issue === 'enregistree' || erreur?.code === 'DEJA_PRESENT') {
    return (
      <section aria-labelledby="titre-presence">
        <h2 id="titre-presence">Marquer sa présence</h2>
        <p className="succes" role="status">
          {issue === 'enregistree' ? 'Présence enregistrée.' : erreur?.message}
        </p>
      </section>
    )
  }

  return (
    <section aria-labelledby="titre-presence">
      <h2 id="titre-presence">Marquer sa présence</h2>
      <form onSubmit={soumettre} className="formulaire-code">
        <label htmlFor={idCode}>Saisissez le code affiché par votre formateur.</label>
        <input
          id={idCode}
          className="saisie-code"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase().slice(0, LONGUEUR_CODE))}
          maxLength={LONGUEUR_CODE}
          autoComplete="off"
          autoCapitalize="characters"
          spellCheck={false}
          inputMode="text"
          aria-describedby={`${idCode}-note`}
        />
        <MessageErreur erreur={erreur} />
        <button type="submit" disabled={enCours || code.length === 0}>
          {enCours ? 'Validation…' : 'Valider ma présence'}
        </button>
        <p id={`${idCode}-note`} className="notes">
          Le code est valable 15 minutes après l'ouverture de la séance. En cas de problème, votre formateur peut vous
          ajouter à la main.
        </p>
      </form>
    </section>
  )
}
