import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  Button,
  Form,
  FormGroup,
  FormText,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import FloatingLabelInput from '../FloatingLabelInput'

class IframeAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {
      editing: false,
      height: '',
      src: '',
      title: '',
      width: '',
    }
    subscribe('editor.show-iframe-modal', (_msg, data = {}) => {
      this.setState({
        ...data,
        title: data.title ?? '',
      })
      this.props.onToggle()
    })
  }

  addIframeToEditor = () => {
    const filteredState = Object.fromEntries(
      Object.entries(this.state).filter(
        ([key, value]) => key !== 'editing' && value !== ''
      )
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
    const {isOpen, t} = this.props
    const {editing, height, src, title, width} = this.state
    return (
      <Modal isOpen={isOpen} toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {editing ? t('insert.iframe.edit') : t('insert.iframe.new')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              this.addIframeToEditor()
            }}
          >
            <FormGroup>
              <FloatingLabelInput
                label="URL"
                name="iframe-url"
                onChange={(value) => this.setState({src: value})}
                required
                type="url"
                value={src}
              />
            </FormGroup>
            <FormGroup>
              <FloatingLabelInput
                label={t('width')}
                name="iframe-width"
                onChange={(value) => this.setState({width: value})}
                value={width}
              />
              <FormText className="ml-1">
                {t('forExample')}
                <code className="ml-1">500</code>, <code>300px</code> or
                <code className="ml-1">50%</code>
              </FormText>
            </FormGroup>
            <FormGroup>
              <FloatingLabelInput
                label={t('height')}
                name="iframe-height"
                onChange={(value) => this.setState({height: value})}
                value={height}
              />
              <FormText className="ml-1">
                {t('forExample')}
                <code className="ml-1">500</code>, <code>300px</code> or
                <code className="ml-1">50%</code>
              </FormText>
            </FormGroup>
            <FormGroup>
              <FloatingLabelInput
                label={t('title')}
                name="iframe-title"
                onChange={(value) => this.setState({title: value})}
                value={title}
              />
            </FormGroup>
            <Button className="float-right" color="primary" type="submit">
              {editing ? t('save') : t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(IframeAdderModal)
