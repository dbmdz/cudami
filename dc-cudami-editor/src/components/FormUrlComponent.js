import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';

const FormUrlComponent = (props) => {
  return (
    <FormGroup>
      <Label className='font-weight-bold' for='url'>URL</Label>
      <Input id='url' name='url' onChange={props.onChange} type='text' value={props.url} />
    </FormGroup>
  )
};

export default FormUrlComponent;
