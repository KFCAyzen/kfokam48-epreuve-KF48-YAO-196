import AvecIdentite from '../identite/AvecIdentite'

/** Écran étudiant (F2) : l'étudiant se désigne, puis marque sa présence et dépose son exercice. */
export default function EtudiantPage() {
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">{identite.promotionNom}</p>
          <h1>Bonjour {identite.nom}</h1>
        </>
      )}
    </AvecIdentite>
  )
}
