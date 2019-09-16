import React from 'react';
import {
  FormGroup,
  Input,
  Label
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const FormLabelComponent = (props) => {
  const { t } = useTranslation();
  return (
    <FormGroup>
      <Label className='font-weight-bold' for={'label-' + props.language}>{t('label')}</Label>
      <Input
        id={'label-' + props.language}
        onChange={props.onUpdate}
        type='text'
        value={props.label}
      />
    </FormGroup>
  )
};

export default FormLabelComponent;
