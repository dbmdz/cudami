import ReactDOM from 'react-dom'

import IdentifiableForm from '../components/forms/IdentifiableForm'
import initI18n from '../i18n'

export default function ({
  activeLanguage,
  apiContextPath,
  existingLanguages,
  id,
  parentType,
  parentUuid,
  parentWebsite,
  type,
  uiLocale,
  uuid,
}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <IdentifiableForm
      activeLanguage={activeLanguage}
      apiContextPath={apiContextPath}
      existingLanguages={existingLanguages}
      parentType={parentType}
      parentUuid={parentUuid}
      parentWebsite={parentWebsite}
      type={type}
      uiLocale={uiLocale}
      uuid={uuid}
    />,
    document.getElementById(id),
  )
}
