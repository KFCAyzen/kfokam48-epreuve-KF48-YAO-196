import { useEffect, useRef, useState } from 'react'
import { ApiError } from '../api/http'

type Resultat<T> = { cle: string; donnees?: T; erreur?: ApiError }

type Etat<T> = {
  donnees: T | undefined
  chargement: boolean
  erreur: ApiError | undefined
  recharger: () => void
}

function versApiError(e: unknown): ApiError {
  return e instanceof ApiError ? e : new ApiError(0, 'INCONNUE', String(e))
}

/**
 * Charge une ressource de l'API et expose les états de chargement et d'erreur (F3).
 * La ressource est rechargée quand `cle` change ; `cle = null` suspend l'appel
 * (par exemple tant qu'aucune promotion n'est choisie).
 */
export function useApi<T>(cle: string | null, chargeur: () => Promise<T>): Etat<T> {
  const chargeurCourant = useRef(chargeur)
  const [version, setVersion] = useState(0)
  const [resultat, setResultat] = useState<Resultat<T>>()
  const cleComplete = cle === null ? null : `${cle}#${version}`

  useEffect(() => {
    chargeurCourant.current = chargeur
  })

  useEffect(() => {
    if (cleComplete === null) return
    let annule = false
    chargeurCourant
      .current()
      .then((donnees) => {
        if (!annule) setResultat({ cle: cleComplete, donnees })
      })
      .catch((e: unknown) => {
        if (!annule) setResultat({ cle: cleComplete, erreur: versApiError(e) })
      })
    return () => {
      annule = true
    }
  }, [cleComplete])

  const courant = resultat?.cle === cleComplete ? resultat : undefined
  // Pendant un rechargement de la même ressource, on garde l'affichage précédent.
  const precedent = cle !== null && resultat?.cle.startsWith(`${cle}#`) ? resultat : undefined

  return {
    donnees: (courant ?? precedent)?.donnees,
    erreur: courant?.erreur,
    chargement: cleComplete !== null && courant === undefined,
    recharger: () => setVersion((v) => v + 1),
  }
}
