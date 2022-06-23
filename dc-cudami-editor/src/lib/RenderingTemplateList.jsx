import ReactDOM from 'react-dom'

import PagedRenderingTemplateList from '../components/lists/PagedRenderingTemplateList'
import initI18n from '../i18n'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedRenderingTemplateList
      apiContextPath={apiContextPath}
      uiLocale={uiLocale}
    />,
    document.getElementById(id),
  )
}
