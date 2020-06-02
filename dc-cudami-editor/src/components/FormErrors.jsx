import React from 'react'
import {Alert} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FormErrors = ({invalidLanguages}) => {
  const {t} = useTranslation()
  return (
    <Alert color="danger">
      {t('invalidLabels')}
      <ul className="mb-0">
        {invalidLanguages.map((language) => (
          <li key={language}>{t(`languageNames:${language}`)}</li>
        ))}
      </ul>
    </Alert>
  )
}

export default FormErrors
