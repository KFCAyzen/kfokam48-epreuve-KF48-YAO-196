import AvecIdentite from '../identite/AvecIdentite'

/** Écran relecteur (F2) : le relecteur est un étudiant ; il se désigne par le même écran (spécification 05). */
export default function RelecteurPage() {
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">Relecture entre pairs</p>
          <h1>Relectures assignées à {identite.nom}</h1>
        </>
      )}
    </AvecIdentite>
  )
}
