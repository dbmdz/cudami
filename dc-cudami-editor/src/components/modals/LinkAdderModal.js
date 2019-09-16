import {
  publish,
  subscribe
} from 'pubsub-js';
import React, { Component } from 'react';
import {
  Button,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader
} from 'reactstrap';
import { withTranslation } from 'react-i18next';

class LinkAdderModal extends Component {
  constructor(props){
    super(props);
    this.state = {
      href: '',
      title: ''
    };
    subscribe('editor.show-link-modal', () => {
      this.props.onToggle();
    });
  }

  addLinkToEditor = () => {
    this.props.onToggle();
    publish('editor.add-link', this.state);
    this.setState({
      href: '',
      title: ''
    });
  };

  render(){
    const { t } = this.props;
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>{t('insertLink')}</ModalHeader>
        <ModalBody>
          <FormGroup>
            <Input
              onChange={evt => this.setState({href: evt.target.value})}
              placeholder='URL'
              type='url'
              value={this.state.href}
            />
          </FormGroup>
          <FormGroup className="mb-0">
            <Input
              onChange={evt => this.setState({title: evt.target.value})}
              placeholder={t('label')}
              type='text'
              value={this.state.height}
            />
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color='primary' onClick={this.addLinkToEditor}>{t('add')}</Button>
        </ModalFooter>
      </Modal>
    );
  }
}

export default withTranslation()(LinkAdderModal);
