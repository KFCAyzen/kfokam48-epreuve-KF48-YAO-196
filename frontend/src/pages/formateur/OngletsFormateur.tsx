import { NavLink } from 'react-router-dom'

/** Navigation interne de l'écran formateur : ouvrir une session (01) et tableau de la promotion (03). */
export default function OngletsFormateur() {
  return (
    <nav className="onglets" aria-label="Écran formateur">
      <NavLink to="/formateur" end>
        Sessions
      </NavLink>
      <NavLink to="/formateur/tableau">Tableau de la promotion</NavLink>
    </nav>
  )
}
