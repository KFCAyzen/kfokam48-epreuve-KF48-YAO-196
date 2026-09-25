import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom'
import Accueil from './pages/Accueil'
import EtudiantPage from './pages/EtudiantPage'
import FormateurPage from './pages/FormateurPage'
import RelecteurPage from './pages/RelecteurPage'

export default function App() {
  return (
    <BrowserRouter>
      <header className="entete">
        <NavLink to="/" end>
          KFOKAM48
        </NavLink>
        <nav>
          <NavLink to="/formateur">Formateur</NavLink>
          <NavLink to="/etudiant">Étudiant</NavLink>
          <NavLink to="/relecteur">Relecteur</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Accueil />} />
          <Route path="/formateur" element={<FormateurPage />} />
          <Route path="/etudiant" element={<EtudiantPage />} />
          <Route path="/relecteur" element={<RelecteurPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}
