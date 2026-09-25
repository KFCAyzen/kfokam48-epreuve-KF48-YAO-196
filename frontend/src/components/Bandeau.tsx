import { Link, NavLink } from 'react-router-dom'

type Props = {
  /** Qui utilise l'écran, affiché à droite (ex. « Formateur » ou le nom de l'étudiant). */
  qui?: string
  /** Contexte sous le nom (ex. la promotion). */
  contexte?: string
  /** Si fourni, affiche « Changer d'identité » (écrans étudiant et relecteur). */
  onChangerIdentite?: () => void
}

/** Bandeau commun aux trois écrans, repris du composant « Masthead » de la maquette. */
export default function Bandeau({ qui, contexte, onChangerIdentite }: Props) {
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
      {qui ? (
        <div className="bandeau-qui">
          <strong>{qui}</strong>
          {contexte ? <span>{contexte}</span> : null}
          {onChangerIdentite ? (
            <button type="button" className="lien" onClick={onChangerIdentite}>
              Changer d'identité
            </button>
          ) : null}
        </div>
      ) : null}
    </header>
  )
}
