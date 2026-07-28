/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Brand colors - sẽ cập nhật từ Figma
        primary: {
          50: '#FFF1EE',
          100: '#FFE0D3',
          200: '#FFC5AD',
          300: '#FF9E6E',
          400: '#FF7A42',
          500: '#FF6B35', // Primary brand color
          600: '#E55A2B',
          700: '#C24521',
          800: '#9E361B',
          900: '#812D16'
        },
        secondary: {
          50: '#F0F9FF',
          100: '#E0F2FE',
          200: '#BAE6FD',
          300: '#7DD3FC',
          400: '#38BDF8',
          500: '#0EA5E9',
          600: '#0284C7',
          700: '#0369A1',
          800: '#075985',
          900: '#0C4A6E'
        }
      },
      fontFamily: {
        geist: ['Geist', 'sans-serif'],
        sans: ['Geist', 'sans-serif'],
        mono: ['Geist Mono', 'JetBrains Mono', 'monospace'],
        'geist-mono': ['Geist Mono', 'monospace'],
        'jetbrains-mono': ['JetBrains Mono', 'monospace'],
        'dancing-script': ['Dancing Script', 'cursive'],
      },
      spacing: {
        '128': '32rem',
      }
    },
  },
  plugins: [
    // Plugins will be added when needed
    // require('@tailwindcss/forms'),
    // require('@tailwindcss/typography'),
  ],
}
