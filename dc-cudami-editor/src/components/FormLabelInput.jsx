import React from 'react'
import {FormGroup, Input, Label} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FormLabelInput = ({label, language, onUpdate}) => {
  const {t} = useTranslation()
  return (
    <FormGroup>
      <Label className="font-weight-bold" for={`label-${language}`}>
        {t('label')}
      </Label>
      <Input
        id={`label-${language}`}
        onChange={onUpdate}
        type="text"
        value={label}
      />
    </FormGroup>
  )
}

export default FormLabelInput
