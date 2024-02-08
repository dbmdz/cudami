import ReactDOM from 'react-dom'

import PagedRenderingTemplateList from '../components/lists/PagedRenderingTemplateList'
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
      <PagedRenderingTemplateList />
    </Store>,
    document.getElementById(id),
  )
}
