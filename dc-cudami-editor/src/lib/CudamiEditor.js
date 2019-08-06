import React from 'react';
import ReactDOM from 'react-dom';
import 'babel-polyfill';

import IdentifiableForm from '../components/IdentifiableForm';

export default function (config) {
  ReactDOM.render(
    <IdentifiableForm
      activeLocale={config.activeLocale}
      debug={config.debug}
      type={config.type}
      uiLocale={config.uiLocale}
      uuid={config.uuid}
    />,
    document.getElementById(config.id)
  );
};
