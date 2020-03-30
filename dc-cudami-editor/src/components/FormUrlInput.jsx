import React from 'react'
import {FormGroup, Input, Label} from 'reactstrap'

const FormUrlInput = (props) => {
  return (
    <FormGroup>
      <Label className="font-weight-bold" for="url">
        URL
      </Label>
      <Input
        id="url"
        onChange={props.onChange}
        required="required"
        type="url"
        value={props.url}
      />
    </FormGroup>
  )
}

export default FormUrlInput
