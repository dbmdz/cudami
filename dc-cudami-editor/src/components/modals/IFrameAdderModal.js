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

class IFrameAdderModal extends Component {
  constructor(props){
    super(props);
    this.state = {
      height: '',
      src: '',
      width: ''
    };
    subscribe('editor.show-iframe-modal', () => {
      this.props.onToggle();
    });
  }

  addIframeToEditor = () => {
    this.props.onToggle();
    publish('editor.add-iframe', this.state);
    this.setState({
      height: '',
      src: '',
      width: ''
    });
  };

  render(){
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>IFrame Hinzufügen</ModalHeader>
        <ModalBody>
          <FormGroup>
            <Input
              onChange={evt => this.setState({src: evt.target.value})}
              placeholder='URL'
              required='required'
              type='text'
              value={this.state.src}
            />
          </FormGroup>
          <FormGroup>
            <Input
              onChange={evt => this.setState({width: evt.target.value})}
              placeholder='Breite'
              required='required'
              type='text'
              value={this.state.width}
            />
          </FormGroup>
          <FormGroup>
            <Input
              onChange={evt => this.setState({height: evt.target.value})}
              placeholder='Höhe'
              required='required'
              type='text'
              value={this.state.height}
            />
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color='primary' onClick={this.addIframeToEditor}>Hinzufügen</Button>
        </ModalFooter>
      </Modal>
    );
  }
}

export default IFrameAdderModal;
