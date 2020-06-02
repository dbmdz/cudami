import React from 'react'
import {FormGroup, Input, Label} from 'reactstrap'

const FormUrlInput = ({onChange, url}) => {
  return (
    <FormGroup>
      <Label className="font-weight-bold" for="url">
        URL
      </Label>
      <Input
        id="url"
        onChange={(evt) => onChange(evt.target.value)}
        required
        type="url"
        value={url}
      />
    </FormGroup>
  )
}

export default FormUrlInput
