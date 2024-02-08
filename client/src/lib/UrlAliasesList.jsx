import ReactDOM from 'react-dom'

import UrlAliases from '../components/UrlAliases'
import initI18n from '../i18n'

export default function ({id, uiLocale, urlAliases: urlAliasesForLanguage}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <UrlAliases
      aliases={urlAliasesForLanguage}
      readOnly={true}
      showAll={true}
    />,
    document.getElementById(id),
  )
}
