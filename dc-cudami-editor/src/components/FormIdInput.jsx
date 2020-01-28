import React from 'react'
import {FormGroup, Input, Label} from 'reactstrap'

const FormIdInput = props => {
  return (
    <FormGroup>
      <Label className="font-weight-bold" for="id">
        ID
      </Label>
      <Input id="id" readOnly type="text" value={props.id} />
    </FormGroup>
  )
}

export default FormIdInput
