import classNames from 'classnames'
import React, {useState} from 'react'
import {Input, Label} from 'reactstrap'

const FloatingLabelInput = ({label, name, onChange, type = 'text', value}) => {
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
        type={type}
        value={value}
      />
      <Label
        className={classNames({
          'floating-label': true,
          'text-primary': focussed,
          'text-secondary': !focussed,
        })}
        for={`${name}-input`}
      >
        {label}
      </Label>
    </div>
  )
}

export default FloatingLabelInput
