/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: {
          dark: '#1A1A2E',
          darker: '#141423',
          card: '#1F2940',
          cardHover: '#263350',
          light: '#F6F8FA',
          lightCard: '#FFFFFF'
        },
        primary: {
          DEFAULT: '#00C896',
          dark: '#00A67E',
          light: '#4DDBBB'
        },
        cyber: {
          blue: '#58A6FF',
          purple: '#8C52FF',
          red: '#F85149',
          green: '#3FB950',
          orange: '#FF7B72',
          yellow: '#E3B341',
          teal: '#2DD4BF'
        }
      }
    },
  },
  plugins: [],
}
