import React from 'react'
import ReactDOM from 'react-dom'

import PagedIdentifiableList from '../components/lists/PagedIdentifiableList'
import initI18n from '../i18n'

export default function ({
  apiContextPath,
  enableAdd,
  enableChangeOfOrder,
  enableMove,
  enableRemove,
  enableSearch,
  existingLanguages,
  id,
  parentType,
  parentUuid,
  showEdit,
  showNew,
  type,
  uiLocale,
}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedIdentifiableList
      apiContextPath={apiContextPath}
      enableAdd={enableAdd}
      enableChangeOfOrder={enableChangeOfOrder}
      enableMove={enableMove}
      enableRemove={enableRemove}
      enableSearch={enableSearch}
      existingLanguages={existingLanguages}
      parentType={parentType}
      parentUuid={parentUuid}
      showEdit={showEdit}
      showNew={showNew}
      type={type}
      uiLocale={uiLocale}
    />,
    document.getElementById(id)
  )
}
