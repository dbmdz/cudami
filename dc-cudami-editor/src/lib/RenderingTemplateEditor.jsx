import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import RenderingTemplateForm from '../components/forms/RenderingTemplateForm'

export default function ({apiContextPath, id, uiLocale, uuid}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <RenderingTemplateForm apiContextPath={apiContextPath} uuid={uuid} />,
    document.getElementById(id)
  )
}
