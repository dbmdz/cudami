import React from 'react';
import ReactDOM from 'react-dom';
import 'babel-polyfill';

import IdentifiableForm from '../components/IdentifiableForm';

export default function (config) {
  ReactDOM.render(
    <IdentifiableForm
      activeLanguage={config.activeLanguage}
      debug={config.debug}
      parentType={config.parentType}
      parentUuid={config.parentUuid}
      type={config.type}
      uiLocale={config.uiLocale}
      uuid={config.uuid}
    />,
    document.getElementById(config.id)
  );
};
