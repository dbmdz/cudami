import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedIdentifierTypeList from '../components/lists/PagedIdentifierTypeList'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedIdentifierTypeList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
