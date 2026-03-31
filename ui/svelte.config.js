import sveltePreprocess from 'svelte-preprocess'

const warningsToIgnore = [
  'a11y_autofocus',
  'a11y_no_onchange',
  'a11y_missing_attribute',
  'a11y_label_has_associated_control',
  'a11y_click_events_have_key_events',
  'security_anchor_rel_noreferrer'
]

const isTest = process.env.NODE_ENV === 'test'

const preprocess = sveltePreprocess({
  postcss: true,
})

export default {
  compilerOptions: {
    accessors: isTest
  },
  preprocess,
  onwarn: (warning, handler) => {
    if (warningsToIgnore.includes(warning.code)) return
    handler(warning)
  }
}
