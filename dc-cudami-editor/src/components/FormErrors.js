import React from 'react';
import {
  Alert
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const FormErrors = (props) => {
  const { t } = useTranslation();
  return (
    <Alert color="danger">
      {t('invalidLabels')}
      <ul className="mb-0">
        {props.invalidLanguages.map(language => <li>{t(`languageNames:${language}`)}</li>)}
      </ul>
    </Alert>
  )
};

export default FormErrors;
