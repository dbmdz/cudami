import ReactDOM from 'react-dom'

import IdentifierTypeForm from '../components/forms/IdentifierTypeForm'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale, uuid}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <IdentifierTypeForm apiContextPath={apiContextPath} uuid={uuid} />,
    document.getElementById(id),
  )
}
