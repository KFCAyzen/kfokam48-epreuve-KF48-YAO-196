import AvecIdentite from '../../identite/AvecIdentite'
import MarquerPresence from './MarquerPresence'

/** Écran étudiant (F2) : l'étudiant se désigne (spécification 05), puis marque sa présence (06). */
export default function EtudiantPage() {
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">{identite.promotionNom}</p>
          <h1>{identite.nom}</h1>
          <MarquerPresence etudiantId={identite.etudiantId} />
        </>
      )}
    </AvecIdentite>
  )
}
