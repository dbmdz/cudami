import classNames from 'classnames'
import React, {useState} from 'react'
import {Input, Label} from 'reactstrap'

const FloatingLabelInput = ({label, name, onChange, value}) => {
  const [focussed, setFocussed] = useState(false)
  return (
    <>
      <Input
        className="text-dark"
        id={`${name}-input`}
        name={name}
        onBlur={() => setFocussed(false)}
        onChange={(evt) => onChange(evt.target.value)}
        onFocus={() => setFocussed(true)}
        type="text"
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
    </>
  )
}

export default FloatingLabelInput
