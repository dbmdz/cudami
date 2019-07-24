import React from 'react';
import {
  Button,
  Input,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

const LocaleAdderModal = (props) => {
  const { t } = useTranslation();
  return (
    <Modal isOpen={props.isOpen} toggle={props.onToggle}>
      <ModalHeader toggle={props.onToggle}>{t('chooseLocale')}</ModalHeader>
      <ModalBody>
        <Input
          onChange={(evt) => props.onSelect(evt.target.value)}
          type='select'
        >
          {props.availableLocales.map((locale) => <option key={locale}>{locale}</option>)}
        </Input>
      </ModalBody>
      <ModalFooter>
        <Button color='primary' onClick={props.onSubmit}>{t('add')}</Button>
      </ModalFooter>
    </Modal>
  );
};

export default LocaleAdderModal;
