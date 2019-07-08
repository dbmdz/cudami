import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';

const FormIdComponent = (props) => {
  return (
    <FormGroup>
      <Label className='font-weight-bold' for='id'>ID</Label>
      <Input id='id' name='id' readOnly='readonly' type='text' defaultValue={props.id} />
    </FormGroup>
  )
};

export default FormIdComponent;
