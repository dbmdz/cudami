import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedGeoLocationsList from '../components/PagedGeoLocationsList'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedGeoLocationsList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
