import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';

const FormUrlInput = (props) => {
  return (
    <FormGroup>
      <Label className='font-weight-bold' for='url'>URL</Label>
      <Input id='url' name='url' onChange={props.onChange} type='url' value={props.url} />
    </FormGroup>
  )
};

export default FormUrlInput;
