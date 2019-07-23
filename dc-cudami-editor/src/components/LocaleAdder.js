import React from 'react';
import { faPlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  NavItem,
  NavLink
} from 'reactstrap';

const LocaleAdder = (props) => {
  return (
    <NavItem>
      <NavLink
        onClick={() => props.onClick('localeAdder')}
        title='Sprache hinzufügen'
      >
        <FontAwesomeIcon icon={faPlus} />
      </NavLink>
    </NavItem>
  );
};

export default LocaleAdder;
