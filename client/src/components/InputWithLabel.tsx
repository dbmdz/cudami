import {useTranslation} from 'react-i18next'
import {FormGroup, Input, Label} from 'reactstrap'
import {InputType} from 'reactstrap/es/Input'

interface Props {
  id: string
  label?: string
  labelKey?: string
  onChange?(label: string): void
  readOnly?: boolean
  required?: boolean
  type?: InputType
  value?: string
}

const InputWithLabel = ({
  id,
  label,
  labelKey,
  onChange,
  readOnly = false,
  required = false,
  type = 'text',
  value = '',
}: Props) => {
  const {t} = useTranslation()
  return (
    <FormGroup>
      <Label className="font-weight-bold" for={id}>
        {labelKey ? t(labelKey) : label}
      </Label>
      <Input
        id={id}
        invalid={required && !value}
        onChange={(evt) => onChange && onChange(evt.target.value)}
        readOnly={readOnly}
        required={required}
        type={type}
        value={value}
      />
    </FormGroup>
  )
}

export default InputWithLabel
