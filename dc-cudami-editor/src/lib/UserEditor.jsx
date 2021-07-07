import ReactDOM from 'react-dom'

import UserForm from '../components/forms/UserForm'
import initI18n from '../i18n'

export default function ({allRoles, apiContextPath, id, uiLocale, uuid}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <UserForm
      apiContextPath={apiContextPath}
      uuid={uuid}
      allRoles={allRoles}
    />,
    document.getElementById(id)
  )
}
