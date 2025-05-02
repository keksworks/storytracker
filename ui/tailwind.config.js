const defaultTheme = require('tailwindcss/defaultTheme')
const production = process.env.NODE_ENV === 'production'
const colors = require('tailwindcss/colors')

module.exports = {
  content: ["./src/**/*.svelte"],
  darkMode: 'class', // or 'media' or 'class'
  theme: {
    extend: {
      fontFamily: {
        serif: 'serif'
      },
      borderRadius: {
        large: '3rem',
      },
      colors: {
        'success': colors.emerald,
        'warning': colors.amber,
        'danger': colors.red,
        'primary': colors.blue,
        'secondary': colors.stone
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ]
}
