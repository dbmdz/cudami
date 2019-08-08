import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';

const FormIdInput = (props) => {
  return (
    <FormGroup>
      <Label className='font-weight-bold' for='id'>ID</Label>
      <Input id='id' name='id' readOnly='readonly' type='text' value={props.id} />
    </FormGroup>
  )
};

export default FormIdInput;
