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

const LanguageAdderModal = (props) => {
  const { t } = useTranslation();
  return (
    <Modal isOpen={props.isOpen} toggle={props.onToggle}>
      <ModalHeader toggle={props.onToggle}>{t('chooseLanguage')}</ModalHeader>
      <ModalBody>
        <Input
          onChange={(evt) => props.onSelect(evt.target.value)}
          type='select'
        >
          {props.availableLanguages.map(language => <option key={language.name} value={language.name}>{language.displayName}</option>)}
        </Input>
      </ModalBody>
      <ModalFooter>
        <Button color='primary' onClick={props.onSubmit}>{t('add')}</Button>
      </ModalFooter>
    </Modal>
  );
};

export default LanguageAdderModal;
