import React from 'react'
import {BrowserRouter as Router, Route} from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.min.css'

import IdentifiableForm from './components/IdentifiableForm'

const getUiLocale = (searchParams) => {
  const query = new URLSearchParams(searchParams)
  return query.get('lang') ?? 'en'
}

const App = () => {
  const availableTypes = [
    'article',
    'collection',
    'corporation',
    'fileResource',
    'project',
    'subtopic',
    'topic',
    'webpage',
    'website',
  ]
  return (
    <Router>
      <Route
        path={`/:type(${availableTypes.join('|')})/new`}
        render={({location, match}) => (
          <IdentifiableForm
            activeLanguage="en"
            debug={true}
            mockApi={true}
            type={match.params.type}
            uiLocale={getUiLocale(location.search)}
          />
        )}
      />
      <Route
        path={`/:type(${availableTypes.join('|')})/edit`}
        render={({location, match}) => (
          <IdentifiableForm
            activeLanguage="en"
            debug={true}
            existingLanguages={['de', 'en']}
            mockApi={true}
            type={match.params.type}
            uiLocale={getUiLocale(location.search)}
            uuid="mock"
          />
        )}
      />
    </Router>
  )
}

export default App
