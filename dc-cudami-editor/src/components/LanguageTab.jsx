import classNames from 'classnames'
import {publish} from 'pubsub-js'
import {useTranslation} from 'react-i18next'
import {FaExclamationCircle, FaTrashAlt} from 'react-icons/fa'
import {Button, NavItem, NavLink} from 'reactstrap'

const LanguageTab = ({
  activeLanguage,
  enableRemove,
  invalid = false,
  language,
  toggle,
}) => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink
        className={classNames('align-items-center', 'd-flex', {
          active: language === activeLanguage,
        })}
        href="#"
        onClick={({currentTarget, target}) => {
          if (currentTarget === target) {
            toggle(language)
          }
        }}
      >
        {invalid && (
          <FaExclamationCircle className="mr-2" color="#dc3545" size="16" />
        )}
        {language ? t(`languageNames:${language}`) : `(${t('notSpecified')})`}
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
