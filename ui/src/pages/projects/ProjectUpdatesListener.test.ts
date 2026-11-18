import {act, render} from '@testing-library/svelte'
import ProjectUpdatesListener from './ProjectUpdatesListener.svelte'
import {project, story} from 'src/api/types'

class FakeEventSource {
  static created: FakeEventSource[] = []
  listenerTypes: string[] = []
  private listeners: Record<string, ((e: MessageEvent) => void)[]> = {}

  constructor(readonly url: string) {
    FakeEventSource.created.push(this)
  }

  addEventListener(type: string, listener: (e: MessageEvent) => void) {
    this.listenerTypes.push(type)
    const listeners = this.listeners[type] ??= []
    listeners.push(listener)
  }

  close() {}

  emit(type: string, data: object) {
    this.listeners[type]?.forEach(l => l({data: JSON.stringify(data)} as MessageEvent))
  }
}

beforeEach(() => {
  FakeEventSource.created = []
  globalThis.EventSource = FakeEventSource as any
})

it('subscribes to story and epic updates on mount', () => {
  render(ProjectUpdatesListener, {project, stories: [], epics: []})
  expect(FakeEventSource.created).to.have.length(1)
  expect(FakeEventSource.created[0].listenerTypes).to.deep.equal(['story', 'epic'])
})

it('passes updated and non-deleted stories to onStoryUpdated', async () => {
  const updated: string[] = []
  render(ProjectUpdatesListener, {
    project, stories: [story], epics: [], onStoryUpdated: s => updated.push(s.name)
  })
  const source = FakeEventSource.created[0]
  await act(() => source.emit('story', {...story, name: 'Updated', updatedAt: '2025-03-03T00:00:01Z'}))
  await act(() => source.emit('story', {...story, status: 'DELETED', updatedAt: '2025-03-03T00:00:02Z'}))
  expect(updated).to.deep.equal(['Updated'])
})
