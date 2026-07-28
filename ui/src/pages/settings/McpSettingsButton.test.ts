import {render} from '@testing-library/svelte'
import McpSettingsButton from './McpSettingsButton.svelte'

it('renders MCP settings button', () => {
  const {container} = render(McpSettingsButton)
  const button = container.querySelector('button')!
  expect(button.textContent).to.contain('AI Agent Access')
})
