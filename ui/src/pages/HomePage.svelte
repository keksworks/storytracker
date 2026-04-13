<script lang="ts">
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
</script>

<MainPageLayout title={t.title}>
  <section>
    <div class="border-b border-stone-200 bg-white px-4 py-8 sm:px-6 sm:py-12">
      <div class="max-w-4xl mx-auto text-center">
        <h1 class="text-3xl font-bold text-stone-900 mb-2">
          {t.home.hero.tagline}
        </h1>
        <p class="text-base text-stone-500">
          {t.home.hero.subtitle}
        </p>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 py-8 sm:px-6 sm:py-10">
      <h2 class="text-xl font-semibold text-stone-900 text-center mb-6">
        {t.home.features.title}
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🔍</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.transparency.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.transparency.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🎯</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.dragAndDrop.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.dragAndDrop.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🌊</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.kanbanFlow.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.kanbanFlow.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🏷️</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.tagViews.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.tagViews.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">📊</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.autoVelocity.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.autoVelocity.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">⚡</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.agileFirst.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.agileFirst.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🗂️</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.epics.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.epics.desc}</p>
        </div>

        <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
          <div class="text-3xl mb-3">🔗</div>
          <h3 class="text-base font-semibold text-stone-900 mb-1">{t.home.features.githubIntegration.title}</h3>
          <p class="text-sm text-stone-500">{t.home.features.githubIntegration.desc}</p>
        </div>
      </div>
    </div>
  </section>
</MainPageLayout>
