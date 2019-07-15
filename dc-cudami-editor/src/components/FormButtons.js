import React from 'react';
import {
  Button,
  ButtonGroup
} from 'reactstrap';

const FormButtons = () => {
  return (
    <div className='float-right'>
      <ButtonGroup>
        <Button className='mr-1' color='secondary'>Abbrechen</Button>
        <Button color='primary'>Speichern</Button>
      </ButtonGroup>
    </div>
  )
};

export default FormButtons;
