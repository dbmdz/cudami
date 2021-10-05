import groupBy from 'lodash/groupBy'
import ReactDOM from 'react-dom'

import UrlAliases from '../components/UrlAliases'
import initI18n from '../i18n'

export default function ({id, uiLocale, urlAliases: urlAliasesForLanguage}) {
  initI18n(uiLocale)
  const aliasesToRender = groupBy(urlAliasesForLanguage, 'website.uuid')
  ReactDOM.render(
    <UrlAliases
      aliases={urlAliasesForLanguage}
      aliasesToRender={aliasesToRender}
      readOnly={true}
    />,
    document.getElementById(id),
  )
}
