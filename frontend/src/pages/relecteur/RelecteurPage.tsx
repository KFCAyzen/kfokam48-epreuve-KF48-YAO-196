import AvecIdentite from '../../identite/AvecIdentite'
import RelecturesAssignees from './RelecturesAssignees'

/** Écran relecteur (F2) : le relecteur est un étudiant ; il se désigne par l'écran 05, puis relit (08). */
export default function RelecteurPage() {
  return (
    <AvecIdentite>
      {(identite) => (
        <>
          <p className="surtitre">Relecture entre pairs</p>
          <h1>Relectures assignées</h1>
          <RelecturesAssignees etudiantId={identite.etudiantId} />
        </>
      )}
    </AvecIdentite>
  )
}
