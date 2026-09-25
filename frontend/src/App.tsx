import { BrowserRouter, Outlet, Route, Routes, useLocation } from 'react-router-dom'
import Bandeau from './components/Bandeau'
import ErreurInattendue from './components/ErreurInattendue'
import { useIdentite } from './identite/contexte'
import IdentiteProvider from './identite/IdentiteProvider'
import Accueil from './pages/Accueil'
import EtudiantPage from './pages/etudiant/EtudiantPage'
import FormateurPage from './pages/formateur/FormateurPage'
import TableauPage from './pages/formateur/TableauPage'
import RelecteurPage from './pages/relecteur/RelecteurPage'

/** Bandeau selon l'écran : « Formateur », ou l'étudiant désigné sur les écrans étudiant et relecteur. */
function BandeauSelonEcran() {
  const { pathname } = useLocation()
  const { identite, oublier } = useIdentite()

  if (pathname.startsWith('/formateur')) return <Bandeau qui="Formateur" />
  if (identite && (pathname.startsWith('/etudiant') || pathname.startsWith('/relecteur'))) {
    return <Bandeau qui={identite.nom} contexte={identite.promotionNom} onChangerIdentite={oublier} />
  }
  return <Bandeau />
}

/** Mise en page commune : bandeau puis l'écran demandé. */
function Gabarit() {
  return (
    <>
      <BandeauSelonEcran />
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
    <IdentiteProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Gabarit />}>
            <Route index element={<Accueil />} />
            <Route path="formateur" element={<FormateurPage />} />
            <Route path="formateur/tableau" element={<TableauPage />} />
            <Route path="etudiant" element={<EtudiantPage />} />
            <Route path="relecteur" element={<RelecteurPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </IdentiteProvider>
  )
}
