import React from 'react';
import {
  Button,
  ButtonGroup
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const FormButtons = () => {
  const { t } = useTranslation();
  return (
    <div className='float-right'>
      <ButtonGroup>
        <Button className='mr-1' color='secondary'>{t('abort')}</Button>
        <Button color='primary'>{t('save')}</Button>
      </ButtonGroup>
    </div>
  )
};

export default FormButtons;
