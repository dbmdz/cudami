import React from 'react'
import {NavItem, NavLink} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaPlus} from 'react-icons/fa'

const LanguageAdder = props => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink
        onClick={() => props.onClick('languageAdder')}
        title={t('addLanguage')}
      >
        <FaPlus />
      </NavLink>
    </NavItem>
  )
}

export default LanguageAdder
