import React from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {initKeepAlive} from './utils/keepAlive.js'

// Initialize backend keep-alive to prevent cold starts
initKeepAlive();

createRoot(document.getElementById('root')).render(
    <React.StrictMode>
    <App />
  </React.StrictMode>
)
