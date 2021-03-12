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
  existingLanguages,
  id,
  parentType,
  parentUuid,
  showEdit,
  showNew,
  showSearch,
  searchTerm,
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
      existingLanguages={existingLanguages}
      parentType={parentType}
      parentUuid={parentUuid}
      showEdit={showEdit}
      showNew={showNew}
      showSearch={showSearch}
      searchTerm={searchTerm}
      type={type}
      uiLocale={uiLocale}
    />,
    document.getElementById(id)
  )
}
