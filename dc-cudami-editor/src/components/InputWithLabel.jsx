import React from 'react'
import {useTranslation} from 'react-i18next'
import {FormGroup, Input, Label} from 'reactstrap'

const InputWithLabel = ({
  id,
  label,
  labelKey,
  onChange = () => {},
  readOnly = false,
  required = false,
  type = 'text',
  value = '',
}) => {
  const {t} = useTranslation()
  return (
    <FormGroup>
      <Label className="font-weight-bold" for={id}>
        {labelKey ? t(labelKey) : label}
      </Label>
      <Input
        id={id}
        onChange={(evt) => onChange(evt.target.value)}
        readOnly={readOnly}
        required={required}
        type={type}
        value={value}
      />
    </FormGroup>
  )
}

export default InputWithLabel
