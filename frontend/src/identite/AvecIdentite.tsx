import type { ReactNode } from 'react'
import Identification from './Identification'
import { useIdentite, type Identite } from './contexte'

type Props = {
  children: (identite: Identite) => ReactNode
}

/** Affiche l'écran demandé si une identité est conservée, sinon l'identification (spécification 05). */
export default function AvecIdentite({ children }: Props) {
  const { identite } = useIdentite()
  return identite ? <>{children(identite)}</> : <Identification />
}
