import React from 'react'
import {useTranslation} from 'react-i18next'
import {Alert} from 'reactstrap'

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
