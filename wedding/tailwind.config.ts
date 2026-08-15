import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./app/**/*.{ts,tsx}', './components/**/*.{ts,tsx}', './lib/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        ivory: {
          50: '#fffdf8',
          100: '#fbf6ea',
          200: '#f6ecd6',
        },
        champagne: {
          100: '#f4e9d8',
          200: '#e9d6b4',
          300: '#ddc191',
        },
        rosegold: {
          200: '#e8c3b9',
          300: '#dba896',
          400: '#c98a76',
          500: '#b8735f',
        },
        persiangold: {
          300: '#e3c081',
          400: '#cda054',
          500: '#b3873e',
          600: '#8f6b2f',
        },
        emerald: {
          400: '#4d8f7b',
          500: '#3a6f5e',
          600: '#2c5548',
        },
        ink: {
          400: '#7a7266',
          500: '#5a5347',
          600: '#3d382f',
          700: '#2a2620',
        },
      },
      fontFamily: {
        calligraphy: ['var(--font-calligraphy)', 'serif'],
        persian: ['var(--font-persian)', 'sans-serif'],
        serif: ['var(--font-serif)', 'serif'],
      },
      backgroundImage: {
        'radial-glow':
          'radial-gradient(60% 60% at 50% 40%, rgba(227,192,129,0.28) 0%, rgba(227,192,129,0) 70%)',
        'silk-gradient':
          'linear-gradient(160deg, #fffdf8 0%, #fbf6ea 35%, #f4e9d8 65%, #e9d6b4 100%)',
      },
      boxShadow: {
        glass: '0 8px 32px 0 rgba(93, 72, 42, 0.12)',
        'glass-lg': '0 20px 60px -10px rgba(93, 72, 42, 0.22)',
        gold: '0 0 24px 0 rgba(205, 160, 84, 0.35)',
      },
      keyframes: {
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0) translateX(0)' },
          '50%': { transform: 'translateY(-14px) translateX(6px)' },
        },
        'pulse-glow': {
          '0%, 100%': { opacity: '0.55' },
          '50%': { opacity: '1' },
        },
      },
      animation: {
        shimmer: 'shimmer 3.5s linear infinite',
        float: 'float 8s ease-in-out infinite',
        'pulse-glow': 'pulse-glow 3.2s ease-in-out infinite',
      },
      transitionTimingFunction: {
        silk: 'cubic-bezier(0.22, 1, 0.36, 1)',
        velvet: 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
    },
  },
  plugins: [],
};

export default config;
