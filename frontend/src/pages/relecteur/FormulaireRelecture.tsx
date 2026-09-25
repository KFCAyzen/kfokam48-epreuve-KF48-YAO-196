import { useId, useState, type FormEvent } from 'react'
import { rendreRelecture } from '../../api/relectures'
import type { RelectureAssignee } from '../../api/types'
import { MessageErreur } from '../../components/Etat'
import { useAction } from '../../hooks/useAction'

type Props = {
  relecture: RelectureAssignee
  etudiantId: number
  onRendue: () => void
}

const COMMENTAIRE_MAX = 2000

/** « Virgule française » acceptée ; « 12,5 » devient 12.5 et c'est l'API qui la refuse (RG3). */
function lireNote(saisie: string): number | null {
  const valeur = Number(saisie.trim().replace(',', '.'))
  return saisie.trim() === '' || Number.isNaN(valeur) ? null : valeur
}

/**
 * Spécification 08, « Relecture en cours » (EF6). Aucune règle de note n'est appliquée ici :
 * l'API valide RG3 et RG17 et son message s'affiche sous le champ concerné.
 */
export default function FormulaireRelecture({ relecture, etudiantId, onRendue }: Props) {
  const idNote = useId()
  const idCommentaire = useId()
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const { executer, enCours, erreur } = useAction(rendreRelecture)

  async function soumettre(evenement: FormEvent<HTMLFormElement>) {
    evenement.preventDefault()
    const rendue = await executer(relecture.id, etudiantId, lireNote(note), commentaire)
    if (rendue) onRendue()
  }

  const erreurNote = erreur?.code === 'NOTE_INVALIDE' ? erreur : undefined
  const erreurCommentaire = erreur?.code === 'COMMENTAIRE_INVALIDE' ? erreur : undefined
  const autreErreur = erreur && !erreurNote && !erreurCommentaire ? erreur : undefined

  return (
    <form onSubmit={soumettre} className="panneau formulaire-relecture" aria-label={`Relecture de l'exercice ${relecture.id}`}>
      <p className="surtitre">Lien de l'exercice</p>
      <p>
        <a href={relecture.lien ?? undefined} target="_blank" rel="noreferrer">
          {relecture.lien}
        </a>
      </p>

      <label htmlFor={idNote}>
        Note<sup>1</sup> <span className="chargement">/ 20</span>
      </label>
      <input
        id={idNote}
        inputMode="numeric"
        className="saisie-note"
        value={note}
        onChange={(e) => setNote(e.target.value)}
      />
      <MessageErreur erreur={erreurNote} />

      <label htmlFor={idCommentaire}>Commentaire</label>
      <textarea
        id={idCommentaire}
        value={commentaire}
        maxLength={COMMENTAIRE_MAX}
        onChange={(e) => setCommentaire(e.target.value)}
      />
      <p className="chargement mono">
        {commentaire.length} / {COMMENTAIRE_MAX}
      </p>
      <MessageErreur erreur={erreurCommentaire} />
      <MessageErreur erreur={autreErreur} />

      <button type="submit" disabled={enCours}>
        {enCours ? 'Envoi…' : 'Rendre la relecture'}
      </button>
      <div className="notes">
        <p>
          <sup>1</sup> Note entière de 0 à 20.
        </p>
        <p>
          <sup>2</sup> Une relecture rendue est définitive et ne peut plus être modifiée.
        </p>
      </div>
    </form>
  )
}
