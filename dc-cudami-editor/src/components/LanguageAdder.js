import React from 'react';
import { faPlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  NavItem,
  NavLink
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const LanguageAdder = (props) => {
  const { t } = useTranslation();
  return (
    <NavItem>
      <NavLink
        onClick={() => props.onClick('languageAdder')}
        title={t('addLanguage')}
      >
        <FontAwesomeIcon icon={faPlus} />
      </NavLink>
    </NavItem>
  );
};

export default LanguageAdder;
