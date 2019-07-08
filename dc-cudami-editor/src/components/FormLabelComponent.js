import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';

const FormLabelComponent = (props) => {
  return (
    <FormGroup>
      <Label className='font-weight-bold' for={'label-' + props.locale}>Titel</Label>
      <Input
        id={'label-' + props.locale}
        name={'label-' + props.locale}
        onChange={props.updateLabel}
        type='text'
        value={props.label}
      />
    </FormGroup>
  )
};

export default FormLabelComponent;
