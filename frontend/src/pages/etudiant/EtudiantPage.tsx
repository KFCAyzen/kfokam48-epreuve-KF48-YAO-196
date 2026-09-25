import { useState } from 'react'
import AvecIdentite from '../../identite/AvecIdentite'
import DeposerExercice from './DeposerExercice'
import MarquerPresence from './MarquerPresence'
import MesExercices from './MesExercices'

/** Écran étudiant (F2) : se désigner (05), marquer sa présence (06), déposer et suivre ses exercices (07). */
export default function EtudiantPage() {
  const [depots, setDepots] = useState(0)
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">{identite.promotionNom}</p>
          <h1>{identite.nom}</h1>
          <MarquerPresence etudiantId={identite.etudiantId} />
          <DeposerExercice
            etudiantId={identite.etudiantId}
            promotionId={identite.promotionId}
            onDepose={() => setDepots((n) => n + 1)}
          />
          <MesExercices etudiantId={identite.etudiantId} version={depots} />
        </>
      )}
    </AvecIdentite>
  )
}
