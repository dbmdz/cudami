import React from 'react'
import 'bootstrap/dist/css/bootstrap.min.css'

import IdentifiableForm from './components/IdentifiableForm'

const App = () => {
  return (
    <IdentifiableForm
      activeLanguage="en"
      debug={true}
      mockApi={true}
      type="webpage"
      uiLocale="en"
      uuid="mock"
    />
  )
}

export default App
