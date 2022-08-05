import ReactDOM from 'react-dom'

import LicenseForm from '../components/forms/LicenseForm'
import initI18n from '../i18n'
import Store from '../state/Store'

export default function ({
  activeLanguage,
  apiContextPath,
  existingLanguages,
  id,
  uiLocale,
  uuid,
}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <Store
      activeLanguage={activeLanguage}
      apiContextPath={apiContextPath}
      existingLanguages={existingLanguages}
      type="form"
      uiLocale={uiLocale}
    >
      <LicenseForm uuid={uuid} />
    </Store>,
    document.getElementById(id),
  )
}
