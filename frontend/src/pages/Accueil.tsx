import { Link } from 'react-router-dom'
import { listerPromotions } from '../api/promotions'
import { Chargement, MessageErreur } from '../components/Etat'
import { useApi } from '../hooks/useApi'

export default function Accueil() {
  const { donnees: promotions, chargement, erreur } = useApi('promotions', listerPromotions)

  return (
    <section>
      <h1>Présence et relecture entre pairs</h1>
      <p>Choisis ton écran :</p>
      <ul className="choix-ecran">
        <li>
          <Link to="/formateur">Formateur</Link> : ouvrir une session, voir le tableau
        </li>
        <li>
          <Link to="/etudiant">Étudiant</Link> : marquer sa présence, déposer son exercice
        </li>
        <li>
          <Link to="/relecteur">Relecteur</Link> : relire l'exercice d'un pair
        </li>
      </ul>

      <h2>Promotions</h2>
      {chargement && <Chargement />}
      <MessageErreur erreur={erreur} />
      <ul>
        {promotions?.map((p) => (
          <li key={p.id}>{p.nom}</li>
        ))}
      </ul>
    </section>
  )
}
