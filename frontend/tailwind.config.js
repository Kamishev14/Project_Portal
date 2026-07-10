/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#00f54dc7',
          100: '#00f54dc7',
          500: '#ffffffff',
          600: '#ffffffff',
          700: '#ffffffff',
        }
      }
    }
  },
  plugins: []
}
