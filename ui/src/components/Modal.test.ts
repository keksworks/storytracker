import {render} from '@testing-library/svelte'
import Modal from './Modal.svelte'
import {tick, unmount} from 'svelte'

it('Modal is shown', async () => {
  const {container, component} = render(Modal, {title: 'Title', show: false, flyParams: {duration: 0}})
  expect(container.textContent).not.to.contain('Title')

  component.show = true
  await tick()
  expect(document.body.textContent).to.contain('Title')
  expect(document.body.classList.contains('modal-open')).to.be.true

  component.show = false
  await tick()
  expect(document.body.classList.contains('modal-open')).to.be.false
})

it('body.modal-open is added on show and removed on destroy', async () => {
  const {component} = render(Modal, {title: 'Title', show: true, flyParams: {duration: 0}})
  expect(document.body.classList.contains('modal-open')).to.be.true
  await unmount(component)
  expect(document.body.classList.contains('modal-open')).to.be.false
  expect(document.querySelector('.modal')).not.to.exist
})
