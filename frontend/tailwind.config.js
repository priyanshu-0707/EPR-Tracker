/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: '#0D1117',
        surface: '#161B22',
        'surface-2': '#1C2330',
        border: '#30363D',
        green: '#3FB950',
        amber: '#D29922',
        red: '#F85149',
        blue: '#58A6FF',
        purple: '#BC8CFF',
        text: '#E6EDF3',
        'text-muted': '#8B949E',
        'text-dim': '#484F58',
      },
      fontFamily: {
        sans: ['"IBM Plex Sans"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
      borderRadius: {
        card: '8px',
        input: '6px',
        badge: '4px',
        pill: '20px',
      }
    },
  },
  plugins: [],
}
