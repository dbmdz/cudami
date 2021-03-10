import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedRenderingTemplateList from '../components/lists/PagedRenderingTemplateList'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedRenderingTemplateList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
