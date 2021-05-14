import {publish} from 'pubsub-js'
import React from 'react'
import {useTranslation} from 'react-i18next'
import {FaTrashAlt} from 'react-icons/fa'
import {Button, NavItem, NavLink} from 'reactstrap'

const LanguageTab = ({activeLanguage, enableRemove, language, toggle}) => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink
        className={language === activeLanguage ? 'active' : ''}
        href="#"
        onClick={({currentTarget, target}) => {
          if (currentTarget === target) {
            toggle(language)
          }
        }}
      >
        {t(`languageNames:${language}`)}
        {enableRemove && (
          <Button
            className="ml-2 p-0"
            color="link"
            onClick={() =>
              publish('editor.show-remove-language-dialog', language)
            }
          >
            <FaTrashAlt />
          </Button>
        )}
      </NavLink>
    </NavItem>
  )
}

export default LanguageTab
