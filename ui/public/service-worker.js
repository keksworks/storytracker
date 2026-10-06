const CACHE_NAME = 'storytracker-v1'

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll([
    '/',
    '/manifest.webmanifest',
    '/img/logo.png',
    '/img/logo-192.png',
    '/img/logo-512.png'
  ])))
  self.skipWaiting()
})

self.addEventListener('activate', event => {
  event.waitUntil(caches.keys().then(keys =>
    Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key)))))
  self.clients.claim()
})

self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET' || new URL(event.request.url).origin !== self.location.origin)
    return

  event.respondWith(fetch(event.request).then(response => {
    if (response.ok) {
      const copy = response.clone()
      caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy))
    }
    return response
  }).catch(() => caches.match(event.request).then(response =>
    response || (event.request.mode === 'navigate' ? caches.match('/') : Response.error()))))
})
