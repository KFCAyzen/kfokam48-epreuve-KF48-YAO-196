import { BrowserRouter, Outlet, Route, Routes, useLocation } from 'react-router-dom'
import Bandeau from './components/Bandeau'
import ErreurInattendue from './components/ErreurInattendue'
import Accueil from './pages/Accueil'
import EtudiantPage from './pages/EtudiantPage'
import FormateurPage from './pages/formateur/FormateurPage'
import RelecteurPage from './pages/RelecteurPage'

/** Mise en page commune : bandeau puis l'écran demandé. */
function Gabarit() {
  const { pathname } = useLocation()
  return (
    <>
      <Bandeau qui={pathname.startsWith('/formateur') ? 'Formateur' : undefined} />
      <main>
        <ErreurInattendue>
          <Outlet />
        </ErreurInattendue>
      </main>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Gabarit />}>
          <Route index element={<Accueil />} />
          <Route path="formateur" element={<FormateurPage />} />
          <Route path="etudiant" element={<EtudiantPage />} />
          <Route path="relecteur" element={<RelecteurPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
