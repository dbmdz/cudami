import './InputWithFloatingLabel.css'

import classNames from 'classnames'
import {useState} from 'react'
import {Input, Label} from 'reactstrap'

const InputWithFloatingLabel = ({
  label,
  name,
  onChange,
  pattern,
  required,
  type = 'text',
  value = '',
}) => {
  const [focussed, setFocussed] = useState(false)
  return (
    <div className="border-0 form-control p-0 position-relative">
      <Input
        className="text-dark"
        id={`${name}-input`}
        name={name}
        onBlur={() => setFocussed(false)}
        onChange={(evt) => onChange(evt.target.value)}
        onFocus={() => setFocussed(true)}
        pattern={pattern}
        required={required}
        type={type}
        value={value}
      />
      <Label
        className={classNames('floating-label', {
          'text-muted': !value && !focussed,
          'text-primary': focussed,
          'text-secondary': value && !focussed,
        })}
        for={`${name}-input`}
      >
        {label}
      </Label>
    </div>
  )
}

export default InputWithFloatingLabel
