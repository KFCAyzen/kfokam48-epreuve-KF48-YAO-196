import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import Bandeau from './components/Bandeau'
import Accueil from './pages/Accueil'
import EtudiantPage from './pages/EtudiantPage'
import FormateurPage from './pages/FormateurPage'
import RelecteurPage from './pages/RelecteurPage'

function BandeauSelonEcran() {
  const { pathname } = useLocation()
  return <Bandeau qui={pathname.startsWith('/formateur') ? 'Formateur' : undefined} />
}

export default function App() {
  return (
    <BrowserRouter>
      <BandeauSelonEcran />
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
