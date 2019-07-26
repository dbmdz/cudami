import React from 'react';
import {
  Button,
  ButtonGroup
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const FormButtons = (props) => {
  const { t } = useTranslation();
  return (
    <div className='float-right'>
      <ButtonGroup>
        <Button
          className='mr-1'
          color='secondary'
        >
          {t('abort')}
        </Button>
        <Button
          color='primary'
          onClick={() => props.onSave()}
        >
          {t('save')}
        </Button>
      </ButtonGroup>
    </div>
  )
};

export default FormButtons;
