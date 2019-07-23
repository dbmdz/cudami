import React from 'react';
import {
  Button,
  Input,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader
} from 'reactstrap';

const LocaleAdderModal = (props) => {
  return (
    <Modal isOpen={props.isOpen} toggle={props.onToggle}>
      <ModalHeader toggle={props.onToggle}>Sprache auswählen</ModalHeader>
      <ModalBody>
        <Input
          onChange={(evt) => props.onSelect(evt.target.value)}
          type='select'
        >
          {props.availableLocales.map((locale) => <option key={locale}>{locale}</option>)}
        </Input>
      </ModalBody>
      <ModalFooter>
        <Button color='primary' onClick={props.onSubmit}>Hinzufügen</Button>
      </ModalFooter>
    </Modal>
  );
};

export default LocaleAdderModal;
