import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import IdentifiableForm from '../components/IdentifiableForm'

export default function (config) {
  initI18n(config.uiLocale)
  ReactDOM.render(
    <IdentifiableForm
      activeLanguage={config.activeLanguage}
      apiContextPath={config.apiContextPath}
      existingLanguages={config.existingLanguages}
      parentType={config.parentType}
      parentUuid={config.parentUuid}
      type={config.type}
      uuid={config.uuid}
    />,
    document.getElementById(config.id)
  )
}
