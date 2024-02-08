import ReactDOM from 'react-dom'

import PagedIdentifierTypeList from '../components/lists/PagedIdentifierTypeList'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedIdentifierTypeList
      apiContextPath={apiContextPath}
      uiLocale={uiLocale}
    />,
    document.getElementById(id),
  )
}
