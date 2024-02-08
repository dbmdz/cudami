import ReactDOM from 'react-dom'

import SetupAdminForm from '../components/forms/SetupAdminForm'
import UserForm from '../components/forms/UserForm'
import initI18n from '../i18n'

export default function ({
  allRoles,
  apiContextPath,
  id,
  setupAdmin = false,
  uiLocale,
  uuid,
}) {
  initI18n(uiLocale)
  ReactDOM.render(
    setupAdmin ? (
      <SetupAdminForm apiContextPath={apiContextPath} />
    ) : (
      <UserForm
        allRoles={allRoles}
        apiContextPath={apiContextPath}
        uuid={uuid}
      />
    ),
    document.getElementById(id),
  )
}
