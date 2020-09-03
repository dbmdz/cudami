import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedIdentifiableList from '../components/PagedIdentifiableList'

export default function (config) {
  initI18n(config.uiLocale)
  ReactDOM.render(
    <PagedIdentifiableList
      apiContextPath={config.apiContextPath}
      enableAdd={config.enableAdd}
      enableMove={config.enableMove}
      enableRemove={config.enableRemove}
      parentType={config.parentType}
      parentUuid={config.parentUuid}
      showEdit={config.showEdit}
      showNew={config.showNew}
      type={config.type}
      uiLocale={config.uiLocale}
    />,
    document.getElementById(config.id)
  )
}
