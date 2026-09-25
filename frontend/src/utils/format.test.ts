import { describe, expect, it } from 'vitest'
import { date, heure, minuteur, moyenne } from './format'

describe('format', () => {
  it('écrit une heure comme la maquette : « 10 h 15 »', () => {
    expect(heure('2026-09-25T10:15:00Z')).toBe('10 h 15')
  })

  it('écrit une date courte en français', () => {
    expect(date('2026-09-25T10:15:00Z')).toBe('25 sept. 2026')
  })

  it('affiche le temps restant en minutes et secondes, jamais négatif', () => {
    expect(minuteur(12 * 60_000 + 48_000)).toBe('12:48')
    expect(minuteur(5_000)).toBe('0:05')
    expect(minuteur(-1)).toBe('0:00')
  })

  it('affiche la moyenne de l’API à deux décimales, ou « — » quand elle vaut null (RG19)', () => {
    expect(moyenne(14.75)).toBe('14,75')
    expect(moyenne(13.5)).toBe('13,50')
    expect(moyenne(null)).toBe('—')
  })
})
