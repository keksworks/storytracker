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
        <p class="text-xl text-stone-500">
          {t.home.hero.subtitle}
        </p>
      </div>
    </div>

    <div class="max-w-4xl mx-auto px-4 py-8 sm:px-6 sm:py-10">
      <h2 class="text-xl font-semibold text-stone-900 text-center mb-6">
        {t.home.features.title}
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {#each Object.entries(t.home.features) as [featureKey, feature] (featureKey)}
          {#if typeof feature == 'object'}
            <div class="bg-white rounded-lg p-5 shadow border border-stone-200 hover:bg-stone-50">
              <div class="text-3xl mb-3">{feature.icon}</div>
              <h3 class="text-base font-semibold text-stone-900 mb-1">{feature.title}</h3>
              <p class="text-sm text-stone-500">{feature.desc}</p>
            </div>
          {/if}
        {/each}
      </div>
    </div>
  </section>
</MainPageLayout>
