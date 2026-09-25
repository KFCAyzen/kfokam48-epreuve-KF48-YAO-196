import { useCallback, useState } from 'react'

// Clés préfixées et versionnées : un changement de format n'a qu'à changer la version.
const PREFIXE = 'kf48.v1.'

function lire<T>(cle: string, defaut: T): T {
  try {
    const brut = window.localStorage.getItem(PREFIXE + cle)
    return brut === null ? defaut : (JSON.parse(brut) as T)
  } catch {
    return defaut
  }
}

/**
 * État conservé sur cet appareil (localStorage) : promotion choisie, identité de l'étudiant (Q1).
 * Toute lecture ou écriture qui échoue (navigation privée, stockage plein) retombe sur la valeur en mémoire.
 */
export function useMemoire<T>(cle: string, defaut: T): [T, (valeur: T) => void] {
  const [valeur, setValeur] = useState<T>(() => lire(cle, defaut))

  const enregistrer = useCallback(
    (nouvelle: T) => {
      setValeur(nouvelle)
      try {
        if (nouvelle === null || nouvelle === undefined) window.localStorage.removeItem(PREFIXE + cle)
        else window.localStorage.setItem(PREFIXE + cle, JSON.stringify(nouvelle))
      } catch {
        // Stockage indisponible : la valeur reste valable jusqu'au rechargement.
      }
    },
    [cle],
  )

  return [valeur, enregistrer]
}
