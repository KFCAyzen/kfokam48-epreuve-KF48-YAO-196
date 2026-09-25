import { useCallback, useState } from 'react'
import { ApiError } from '../api/http'

type Action<A extends unknown[], R> = {
  executer: (...args: A) => Promise<R | undefined>
  enCours: boolean
  erreur: ApiError | undefined
  effacerErreur: () => void
}

/**
 * Enveloppe une écriture vers l'API (POST, PUT) : état « en cours » pour désactiver le bouton,
 * erreur de l'API à afficher telle quelle (F3). Renvoie undefined si l'appel a échoué.
 */
export function useAction<A extends unknown[], R>(appel: (...args: A) => Promise<R>): Action<A, R> {
  const [enCours, setEnCours] = useState(false)
  const [erreur, setErreur] = useState<ApiError>()

  const executer = useCallback(
    async (...args: A) => {
      setEnCours(true)
      setErreur(undefined)
      try {
        return await appel(...args)
      } catch (e) {
        setErreur(e instanceof ApiError ? e : new ApiError(0, 'INCONNUE', String(e)))
        return undefined
      } finally {
        setEnCours(false)
      }
    },
    [appel],
  )

  const effacerErreur = useCallback(() => setErreur(undefined), [])

  return { executer, enCours, erreur, effacerErreur }
}
