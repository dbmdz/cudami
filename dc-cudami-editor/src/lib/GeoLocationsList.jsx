import React from 'react'
import ReactDOM from 'react-dom'

import PagedGeoLocationsList from '../components/lists/PagedGeoLocationsList'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedGeoLocationsList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
