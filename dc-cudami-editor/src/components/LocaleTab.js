import React from 'react';
import {
  NavItem,
  NavLink
} from 'reactstrap';

const LocaleTab = (props) => {
  return (
    <NavItem>
      <NavLink
        className={props.locale === props.activeLocale ? 'active' : ''}
        onClick={() => props.onClick(props.locale)}
      >
        {props.locale}
      </NavLink>
    </NavItem>
  );
}

export default LocaleTab;
