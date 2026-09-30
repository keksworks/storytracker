import {render} from '@testing-library/svelte'
import SearchPanel from './SearchPanel.svelte'
import {project, story} from 'src/api/types'
import type {ProjectContext} from './context'
import type {StoryHandlers} from 'src/pages/stories/types'

const ctx = {
  ...project,
  members: {},
  epicTags: new Set<string>(),
  isOwner: true,
  canEdit: true
} as ProjectContext

const handlers: StoryHandlers = {onSearch: () => {}, onSaved: () => {}, onDelete: () => {}}

function renderPanel() {
  return render(SearchPanel, {mode: 'input', project: ctx, pastLoaded: true, handlers})
}

it('renders search input', () => {
  const {container} = renderPanel()
  expect(container.querySelector('input[type=search]')).to.exist
})

it('matches short 2-letter terms in name', () => {
  const {component} = renderPanel()
  const s = {...story, name: 'About API'}
  expect(component.storyMatchesSearch(s, 'ab')).to.be.true
  expect(component.storyMatchesSearch(s, 'xy')).to.be.false
})

it('matches 1-char tags like ?', () => {
  const {component} = renderPanel()
  const s = {...story, name: 'Other', tags: ['?']}
  expect(component.storyMatchesSearch(s, '?')).to.be.true
  expect(component.storyMatchesSearch(s, '!')).to.be.false
})

it('matches by id with or without hash', () => {
  const {component} = renderPanel()
  expect(component.storyMatchesSearch(story, String(story.id))).to.be.true
  expect(component.storyMatchesSearch(story, '#' + story.id)).to.be.true
})
