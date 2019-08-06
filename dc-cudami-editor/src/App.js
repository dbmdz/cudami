import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

import IdentifiableForm from './components/IdentifiableForm';

const App = () => {
  return (
    <IdentifiableForm
      activeLocale="de"
      debug={true}
      mockApi={true}
      type="webpage"
      uiLocale="de"
      uuid="mock"
    />
  );
};

export default App;
