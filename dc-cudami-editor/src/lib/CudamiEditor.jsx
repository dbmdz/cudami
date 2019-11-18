import React from 'react'
import ReactDOM from 'react-dom'
import 'babel-polyfill'

import IdentifiableForm from '../components/IdentifiableForm'

export default function(config) {
  ReactDOM.render(
    <IdentifiableForm
      activeLanguage={config.activeLanguage}
      apiContextPath={config.apiContextPath}
      debug={config.debug}
      existingLanguages={config.existingLanguages}
      parentType={config.parentType}
      parentUuid={config.parentUuid}
      type={config.type}
      uiLocale={config.uiLocale}
      uuid={config.uuid}
    />,
    document.getElementById(config.id)
  )
}
