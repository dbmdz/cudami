import React from 'react';
import ReactDOM from 'react-dom';
import 'babel-polyfill';
import 'bootstrap/dist/css/bootstrap.min.css';

import IdentifiableForm from '../components/IdentifiableForm';

export default function (config) {
  ReactDOM.render(
    <IdentifiableForm activeLocale={config.activeLocale} type={config.type} uuid={config.uuid} />,
    document.getElementById(config.id)
  );
};
