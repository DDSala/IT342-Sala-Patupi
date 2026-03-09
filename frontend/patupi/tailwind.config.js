/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        charcoal: '#1a1a15', 
        gold: '#d4af37',     
        goldHover: '#b8962e',
        inputBg: '#24241e',  
      },
    },
  },
  plugins: [],
}