import { createContext, useContext } from 'react'

/** L'étudiant qui utilise l'appareil : identité déclarative, sans mot de passe (Q1). */
export type Identite = {
  etudiantId: number
  nom: string
  promotionId: number
  promotionNom: string
}

export type ValeurIdentite = {
  identite: Identite | null
  choisir: (identite: Identite) => void
  oublier: () => void
}

export const IdentiteContext = createContext<ValeurIdentite | null>(null)

export function useIdentite(): ValeurIdentite {
  const valeur = useContext(IdentiteContext)
  if (valeur === null) throw new Error('useIdentite doit être utilisé sous <IdentiteProvider>')
  return valeur
}
