import { Component, type ErrorInfo, type ReactNode } from 'react'

type Props = { children: ReactNode }
type Etat = { enErreur: boolean }

/** Barrière d'erreur : une erreur de rendu affiche un message au lieu d'une page blanche. */
export default class ErreurInattendue extends Component<Props, Etat> {
  state: Etat = { enErreur: false }

  static getDerivedStateFromError(): Etat {
    return { enErreur: true }
  }

  componentDidCatch(erreur: Error, info: ErrorInfo) {
    console.error('Erreur de rendu', erreur, info.componentStack)
  }

  render() {
    if (this.state.enErreur) {
      return (
        <p className="erreur" role="alert">
          Une erreur inattendue est survenue. Rechargez la page.
        </p>
      )
    }
    return this.props.children
  }
}
