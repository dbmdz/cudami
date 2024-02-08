import ReactDOM from 'react-dom'

import PagedUserList from '../components/lists/PagedUserList'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedUserList apiContextPath={apiContextPath} uiLocale={uiLocale} />,
    document.getElementById(id),
  )
}
