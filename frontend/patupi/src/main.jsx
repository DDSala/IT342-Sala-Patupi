import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Deleted the import './index.css' line
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)