import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedPersonsList from '../components/PagedPersonsList'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedPersonsList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
