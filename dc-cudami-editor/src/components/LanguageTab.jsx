import React from 'react'
import {NavItem, NavLink} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const LanguageTab = ({activeLanguage, language, onClick}) => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink
        className={language === activeLanguage ? 'active' : ''}
        onClick={() => onClick(language)}
      >
        {t(`languageNames:${language}`)}
      </NavLink>
    </NavItem>
  )
}

export default LanguageTab
