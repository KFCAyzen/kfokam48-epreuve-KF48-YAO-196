import AvecIdentite from '../../identite/AvecIdentite'
import DeposerExercice from './DeposerExercice'
import MarquerPresence from './MarquerPresence'

/** Écran étudiant (F2) : l'étudiant se désigne (spécification 05), puis marque sa présence (06) et dépose son exercice (07). */
export default function EtudiantPage() {
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">{identite.promotionNom}</p>
          <h1>{identite.nom}</h1>
          <MarquerPresence etudiantId={identite.etudiantId} />
          <DeposerExercice etudiantId={identite.etudiantId} promotionId={identite.promotionId} />
        </>
      )}
    </AvecIdentite>
  )
}
