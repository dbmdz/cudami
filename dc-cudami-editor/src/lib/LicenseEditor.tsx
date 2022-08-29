import {render} from 'react-dom'

import LicenseForm from '../components/forms/LicenseForm'
import initI18n from '../i18n'
import Store from '../state/Store'

interface Props {
  activeLanguage: string
  apiContextPath: string
  existingLanguages: string[]
  id: string
  uiLocale: string
  uuid: string
}

export default function ({
  activeLanguage,
  apiContextPath,
  existingLanguages,
  id,
  uiLocale,
  uuid,
}: Props) {
  initI18n(uiLocale)
  render(
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
