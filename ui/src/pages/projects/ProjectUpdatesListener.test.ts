import {act, render} from '@testing-library/svelte'
import ProjectUpdatesListener from './ProjectUpdatesListener.svelte'
import {project, story} from 'src/api/types'

class FakeEventSource {
  static readonly CONNECTING = 0
  static readonly OPEN = 1
  static readonly CLOSED = 2
  static created: FakeEventSource[] = []

  readyState = FakeEventSource.OPEN
  onerror?: () => void
  private listeners: Record<string, ((e: MessageEvent) => void)[]> = {}

  constructor(readonly url: string) {
    FakeEventSource.created.push(this)
  }

  addEventListener(type: string, listener: (e: MessageEvent) => void) {
    (this.listeners[type] ??= []).push(listener)
  }

  close() {
    this.readyState = FakeEventSource.CLOSED
  }

  emit(type: string, data: object, lastEventId = '') {
    if (data['updatedAt']) lastEventId = data['updatedAt']
    this.listeners[type]?.forEach(l => l({data: JSON.stringify(data), lastEventId} as MessageEvent))
  }

  fail(readyState = FakeEventSource.CLOSED) {
    this.readyState = readyState
    this.onerror?.()
  }
}

beforeEach(() => {
  vi.useFakeTimers()
  FakeEventSource.created = []
  globalThis.EventSource = FakeEventSource as any
})

afterEach(() => vi.useRealTimers())

function renderListener() {
  return render(ProjectUpdatesListener, {project, stories: [], epics: []})
}

it('reconnects when the browser closes the stream with an error status, like CloudFlare 524', async () => {
  const {unmount} = renderListener()
  const source = FakeEventSource.created[0]
  const updatedAt = '2025-03-03T00:00:01Z'
  await act(() => source.emit('story', {...story, updatedAt}, updatedAt))
  source.fail()
  await act(() => vi.advanceTimersByTimeAsync(5000))
  expect(FakeEventSource.created.length).to.equal(2)
  expect(FakeEventSource.created[1].url).to.contain('?lastEventId=' + encodeURIComponent(updatedAt))
  unmount()
})

it('leaves reconnection to the browser on network errors', async () => {
  const {unmount} = renderListener()
  FakeEventSource.created[0].fail(FakeEventSource.CONNECTING)
  await act(() => vi.advanceTimersByTimeAsync(5000))
  expect(FakeEventSource.created.length).to.equal(1)
  unmount()
})

it('does not reconnect after being destroyed', async () => {
  const {unmount} = renderListener()
  FakeEventSource.created[0].fail()
  unmount()
  await act(() => vi.advanceTimersByTimeAsync(5000))
  expect(FakeEventSource.created.length).to.equal(1)
})

it('applies received story updates', async () => {
  const updated: string[] = []
  const {unmount} = render(ProjectUpdatesListener, {
    project, stories: [story], epics: [], onStoryUpdated: s => updated.push(s.name)
  })
  await act(() => FakeEventSource.created[0].emit('story', {...story, name: 'Updated', updatedAt: '2025-03-03T00:00:01Z'}))
  expect(updated).to.deep.equal(['Updated'])
  unmount()
})
