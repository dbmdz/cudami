import ReactDOM from 'react-dom'

import PagedLicenseList from '../components/lists/PagedLicenseList'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedLicenseList apiContextPath={apiContextPath} uiLocale={uiLocale} />,
    document.getElementById(id),
  )
}
