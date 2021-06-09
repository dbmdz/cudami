import {useTranslation} from 'react-i18next'
import {FaPlus} from 'react-icons/fa'
import {NavItem, NavLink} from 'reactstrap'

const LanguageAdder = ({onClick}) => {
  const {t} = useTranslation()
  return (
    <NavItem>
      <NavLink onClick={onClick} title={t('addLanguage')}>
        <FaPlus />
      </NavLink>
    </NavItem>
  )
}

export default LanguageAdder
