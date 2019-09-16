import {
  publish,
  subscribe
} from 'pubsub-js';
import React, { Component } from 'react';
import {
  Button,
  FormGroup,
  Input,
  Label,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader
} from 'reactstrap';
import { withTranslation } from 'react-i18next';

class TableAdderModal extends Component {
  constructor(props){
    super(props);
    this.state = {
      columns: 2,
      rows: 2
    };
    subscribe('editor.show-table-modal', () => {
      this.props.onToggle();
    });
  }

  addTableToEditor = () => {
    this.props.onToggle();
    publish('editor.add-table', this.state);
    this.setState({
      columns: 2,
      rows: 2
    });
  };

  render(){
    const { t } = this.props;
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>{t('insert.table')}</ModalHeader>
        <ModalBody>
          <FormGroup>
            <Label className='font-weight-bold' for="rows">{t('numberOfRows')}</Label>
            <Input
              id="rows"
              onChange={evt => this.setState({rows: evt.target.value})}
              type='text'
              value={this.state.rows}
            />
          </FormGroup>
          <FormGroup className="mb-0">
            <Label className='font-weight-bold' for="columns">{t('numberOfColumns')}</Label>
            <Input
              id="columns"
              onChange={evt => this.setState({columns: evt.target.value})}
              type='text'
              value={this.state.columns}
            />
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color='primary' onClick={this.addTableToEditor}>{t('add')}</Button>
        </ModalFooter>
      </Modal>
    );
  }
}

export default withTranslation()(TableAdderModal);
