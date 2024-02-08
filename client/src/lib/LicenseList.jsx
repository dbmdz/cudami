import ReactDOM from 'react-dom'

import PagedLicenseList from '../components/lists/PagedLicenseList'
import initI18n from '../i18n'
import Store from '../state/Store'

export default function ({apiContextPath, existingLanguages, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <Store
      apiContextPath={apiContextPath}
      existingLanguages={existingLanguages}
      type="list"
      uiLocale={uiLocale}
    >
      <PagedLicenseList />
    </Store>,
    document.getElementById(id),
  )
}
