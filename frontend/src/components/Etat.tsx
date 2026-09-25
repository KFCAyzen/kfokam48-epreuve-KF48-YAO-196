import type { ApiError } from '../api/http'

export function Chargement({ texte = 'Chargement…' }: { texte?: string }) {
  return (
    <p className="chargement" role="status">
      {texte}
    </p>
  )
}

/** Affiche le message renvoyé par l'API tel quel (ENF8) ; le code reste visible pour le support. */
export function MessageErreur({ erreur }: { erreur: ApiError | undefined }) {
  if (!erreur) return null
  return (
    <p className="erreur" role="alert">
      {erreur.message} <span className="code-erreur">({erreur.code})</span>
    </p>
  )
}

export function MessageSucces({ texte }: { texte: string | undefined }) {
  if (!texte) return null
  return (
    <p className="succes" role="status">
      {texte}
    </p>
  )
}
