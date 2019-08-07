import React from 'react';
import {
  NavItem,
  NavLink
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const LanguageTab = (props) => {
  const { t } = useTranslation();
  return (
    <NavItem>
      <NavLink
        className={props.language === props.activeLanguage ? 'active' : ''}
        onClick={() => props.onClick(props.language)}
      >
        {t(`languageNames:${props.language}`)}
      </NavLink>
    </NavItem>
  );
}

export default LanguageTab;
