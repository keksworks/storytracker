export const pointsLabels = ['0', 'S', 'M', 'L', 'XL']

export function pointsLabel(points: number) {
  return pointsLabels[points] ?? '?'
}
