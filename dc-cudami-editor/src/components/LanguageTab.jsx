import React from 'react'
import {useTranslation} from 'react-i18next'
import {NavItem, NavLink} from 'reactstrap'

const LanguageTab = ({activeLanguage, language, toggle}) => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink
        className={language === activeLanguage ? 'active' : ''}
        href="#"
        onClick={() => toggle(language)}
      >
        {t(`languageNames:${language}`)}
      </NavLink>
    </NavItem>
  )
}

export default LanguageTab
