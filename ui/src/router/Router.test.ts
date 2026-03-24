import {fireEvent, render} from '@testing-library/svelte'
import RouterTest from './RouterTest.svelte'
import {navigate} from 'src/router/index'
import {tick} from 'svelte'

test('render and navigate', async () => {
  const {container} = render(RouterTest)
  expect(container.innerHTML).toContain('Home Page')
  await fireEvent.click(container.querySelector('a')!)
  expect(container.innerHTML).toContain('About Page')
  navigate('/user/42')
  await tick()
  expect(container.innerHTML).toContain('User 42')
})
