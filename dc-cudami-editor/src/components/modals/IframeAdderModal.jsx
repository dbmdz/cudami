import fromEntries from 'object.fromentries'
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
      title: '',
      width: '',
    }
    subscribe('editor.show-iframe-modal', () => {
      this.props.onToggle()
    })
  }

  addIframeToEditor = () => {
    /* TODO: needs more investigation */
    if (!Object.fromEntries) {
      fromEntries.shim()
    }
    const filteredState = Object.fromEntries(
      Object.entries(this.state).filter(([_, value]) => value !== '')
    )
    publish('editor.add-iframe', filteredState)
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      height: '',
      src: '',
      title: '',
      width: '',
    })
  }

  render() {
    const {t} = this.props
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>{t('insert.iframe')}</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              this.addIframeToEditor()
            }}
          >
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({src: evt.target.value})}
                placeholder="URL"
                required
                type="url"
                value={this.state.src}
              />
            </FormGroup>
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({width: evt.target.value})}
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
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({height: evt.target.value})}
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
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({title: evt.target.value})}
                placeholder={t('title')}
                type="text"
                value={this.state.title}
              />
            </FormGroup>
            <Button className="float-right" color="primary" type="submit">
              {t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(IframeAdderModal)
