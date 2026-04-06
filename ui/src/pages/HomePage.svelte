<script lang="ts">
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
</script>

<MainPageLayout title={t.title}>
  <section class="relative overflow-hidden">
    <!-- Hero Section -->
    <div class="bg-gradient-to-br from-indigo-500 to-purple-600 px-6 py-16 sm:px-12 sm:py-24 relative before:absolute before:inset-0 before:bg-[radial-gradient(circle_at_20%_50%,rgba(255,255,255,0.1)_0%,transparent_50%),radial-gradient(circle_at_80%_80%,rgba(255,255,255,0.1)_0%,transparent_50%)] before:pointer-events-none">
      <div class="max-w-6xl mx-auto relative z-10">
        <div class="text-center text-white">
          <h1 class="text-4xl sm:text-5xl lg:text-6xl font-extrabold mb-4 leading-tight drop-shadow-lg">
            {t.home.hero.tagline}
          </h1>
          <p class="text-lg sm:text-xl lg:text-2xl font-light opacity-95">
            {t.home.hero.subtitle}
          </p>
        </div>
      </div>
    </div>

    <!-- Features Grid -->
    <div class="max-w-6xl mx-auto px-6 py-16 sm:px-12 sm:py-24">
      <h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold text-gray-800 text-center mb-12">
        {t.home.features.title}
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">🔍</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.transparency.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.transparency.desc}</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">🎯</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.dragAndDrop.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.dragAndDrop.desc}</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">🌊</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.kanbanFlow.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.kanbanFlow.desc}</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">🏷️</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.tagViews.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.tagViews.desc}</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">📊</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.autoVelocity.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.autoVelocity.desc}</p>
        </div>

        <div class="bg-white rounded-2xl p-8 shadow-md border border-gray-200 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-indigo-500">
          <div class="text-5xl mb-4">⚡</div>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">{t.home.features.agileFirst.title}</h3>
          <p class="text-gray-600 leading-relaxed">{t.home.features.agileFirst.desc}</p>
        </div>
      </div>
    </div>
  </section>
</MainPageLayout>
