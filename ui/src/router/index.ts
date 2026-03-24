import {writable} from 'svelte/store'
import Route from './Route.svelte'
import Router from './Router.svelte'
import Link from './Link.svelte'
import {on} from 'svelte/events'

export const activePath = writable(location.pathname)
export const routeMatched = writable(false)

activePath.subscribe(() => routeMatched.set(false))

export function init() {
  return on(window, 'popstate', () => activePath.set(location.pathname))
}

let refreshNavigate = false
export function refreshOnNextNavigate() {
  refreshNavigate = true
}

export function navigate(path: string, opts = {replace: false}) {
  if (refreshNavigate) return location.href = path
  const f = opts.replace ? history.replaceState : history.pushState
  f.call(history, {}, '', path)
  activePath.set(path.replace(/[?#].*/, ''))
  setTimeout(() => scrollTo({top: 0}))
}

export function router(el: HTMLElement) {
  return {destroy: on(el, 'click', e => {
    if (el.getAttribute('target')) return
    e.preventDefault()
    navigate(el.getAttribute('href')!)
  })}
}

export {Router, Route, Link}
