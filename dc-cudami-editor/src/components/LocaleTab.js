import React from 'react';
import {
  NavItem,
  NavLink
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const LocaleTab = (props) => {
  const { t } = useTranslation();
  return (
    <NavItem>
      <NavLink
        className={props.locale === props.activeLocale ? 'active' : ''}
        onClick={() => props.onClick(props.locale)}
      >
        {t(`languageNames:${props.locale}`)}
      </NavLink>
    </NavItem>
  );
}

export default LocaleTab;
