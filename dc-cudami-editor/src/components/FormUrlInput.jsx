import React from 'react'
import {FormGroup, Input, Label} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FormUrlInput = ({
  labelKey = 'url',
  onChange,
  required = false,
  url = '',
}) => {
  const {t} = useTranslation()
  return (
    <FormGroup>
      <Label className="font-weight-bold" for="url">
        {t(labelKey)}
      </Label>
      <Input
        id="url"
        onChange={(evt) => onChange(evt.target.value)}
        required={required}
        type="url"
        value={url}
      />
    </FormGroup>
  )
}

export default FormUrlInput
