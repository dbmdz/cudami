import ReactDOM from 'react-dom'

import RenderingTemplateForm from '../components/forms/RenderingTemplateForm'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale, uuid}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <RenderingTemplateForm apiContextPath={apiContextPath} uuid={uuid} />,
    document.getElementById(id)
  )
}
