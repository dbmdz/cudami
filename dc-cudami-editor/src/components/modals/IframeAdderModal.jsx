import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  Button,
  Form,
  FormGroup,
  FormText,
  Input,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class IframeAdderModal extends Component {
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
          <Form
            onSubmit={evt => {
              evt.preventDefault()
              this.addIframeToEditor()
            }}
          >
            <FormGroup>
              <Input
                onChange={evt => this.setState({src: evt.target.value})}
                placeholder="URL"
                required
                type="url"
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
              <FormText className="ml-1">
                {t('forExample')}
                <code className="ml-1">500</code>, <code>300px</code> or
                <code className="ml-1">50%</code>
              </FormText>
            </FormGroup>
            <FormGroup className="mb-0">
              <Input
                onChange={evt => this.setState({height: evt.target.value})}
                placeholder={t('height')}
                type="text"
                value={this.state.height}
              />
              <FormText className="ml-1">
                {t('forExample')}
                <code className="ml-1">500</code>, <code>300px</code> or
                <code className="ml-1">50%</code>
              </FormText>
            </FormGroup>
            <Button className="float-right mt-3" color="primary" type="submit">
              {t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(IframeAdderModal)
