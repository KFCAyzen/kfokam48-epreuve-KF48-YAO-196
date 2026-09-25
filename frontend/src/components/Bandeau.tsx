import { Link, NavLink } from 'react-router-dom'

type Props = {
  /** Qui utilise l'écran, affiché à droite (ex. « Formateur » ou le nom de l'étudiant). */
  qui?: string
  /** Contexte sous le nom (ex. la promotion). */
  contexte?: string
}

/** Bandeau commun aux trois écrans, repris du composant « Masthead » de la maquette. */
export default function Bandeau({ qui, contexte }: Props) {
  return (
    <header className="bandeau">
      <Link to="/" className="bandeau-marque">
        <img src="/kfokam48-logo.png" alt="KFOKAM48" />
        <span>Présence &amp; relecture entre pairs</span>
      </Link>
      <nav aria-label="Écrans">
        <NavLink to="/formateur">Formateur</NavLink>
        <NavLink to="/etudiant">Étudiant</NavLink>
        <NavLink to="/relecteur">Relecteur</NavLink>
      </nav>
      {qui && (
        <div className="bandeau-qui">
          <strong>{qui}</strong>
          {contexte && <span>{contexte}</span>}
        </div>
      )}
    </header>
  )
}
