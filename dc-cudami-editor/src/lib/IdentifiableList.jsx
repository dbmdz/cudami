import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedIdentifiableList from '../components/PagedIdentifiableList'

export default function (config) {
  initI18n(config.uiLocale)
  ReactDOM.render(
    <PagedIdentifiableList
      apiContextPath={config.apiContextPath}
      debug={config.debug}
      parentType={config.parentType}
      parentUuid={config.parentUuid}
      type={config.type}
      uiLocale={config.uiLocale}
    />,
    document.getElementById(config.id)
  )
}
