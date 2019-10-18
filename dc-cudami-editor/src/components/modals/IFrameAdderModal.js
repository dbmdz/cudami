import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  Button,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class IFrameAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {
      height: '',
      src: '',
      width: '',
    }
    subscribe('editor.show-iframe-modal', () => {
      this.props.onToggle()
    })
  }

  addIframeToEditor = () => {
    this.props.onToggle()
    publish('editor.add-iframe', this.state)
    this.setState({
      height: '',
      src: '',
      width: '',
    })
  }

  render() {
    const {t} = this.props
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>
          {t('insert.iframe')}
        </ModalHeader>
        <ModalBody>
          <FormGroup>
            <Input
              onChange={evt => this.setState({src: evt.target.value})}
              placeholder="URL"
              type="text"
              value={this.state.src}
            />
          </FormGroup>
          <FormGroup>
            <Input
              onChange={evt => this.setState({width: evt.target.value})}
              placeholder={t('width')}
              type="text"
              value={this.state.width}
            />
          </FormGroup>
          <FormGroup className="mb-0">
            <Input
              onChange={evt => this.setState({height: evt.target.value})}
              placeholder={t('height')}
              type="text"
              value={this.state.height}
            />
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={this.addIframeToEditor}>
            {t('add')}
          </Button>
        </ModalFooter>
      </Modal>
    )
  }
}

export default withTranslation()(IFrameAdderModal)
