import { useEffect, useState } from 'react'

/** Heure courante, rafraîchie à intervalle régulier (par défaut chaque seconde) pour les minuteurs. */
export function useMaintenant(intervalleMs = 1000): number {
  const [maintenant, setMaintenant] = useState(() => Date.now())

  useEffect(() => {
    const minuterie = window.setInterval(() => setMaintenant(Date.now()), intervalleMs)
    return () => window.clearInterval(minuterie)
  }, [intervalleMs])

  return maintenant
}
