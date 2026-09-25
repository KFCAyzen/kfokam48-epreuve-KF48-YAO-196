import { useMemo, type ReactNode } from 'react'
import { useMemoire } from '../hooks/useMemoire'
import { IdentiteContext, type Identite } from './contexte'

/**
 * Partage l'identité entre le bandeau et les écrans étudiant et relecteur. Elle est conservée sur
 * l'appareil jusqu'à « Changer d'identité » ; l'API reste seule juge de ce que l'étudiant peut faire.
 */
export default function IdentiteProvider({ children }: { children: ReactNode }) {
  const [identite, setIdentite] = useMemoire<Identite | null>('identite', null)
  const valeur = useMemo(
    () => ({ identite, choisir: setIdentite, oublier: () => setIdentite(null) }),
    [identite, setIdentite],
  )
  return <IdentiteContext.Provider value={valeur}>{children}</IdentiteContext.Provider>
}
