import mapValues from 'lodash/mapValues'
import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  Button,
  Form,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import FloatingLabelInput from '../FloatingLabelInput'

class LinkAdderModal extends Component {
  initialAttributes = {
    href: '',
    title: '',
  }

  constructor(props) {
    super(props)
    this.state = {
      attributes: this.initialAttributes,
      editing: false,
    }
    subscribe(
      'editor.show-link-modal',
      (_msg, {attributes = {}, editing = false} = {}) => {
        this.setState({
          attributes: {
            ...this.state.attributes,
            ...mapValues(attributes, (value) => value ?? ''),
          },
          editing,
        })
        this.props.onToggle()
      }
    )
  }

  addLinkToEditor = () => {
    publish(
      'editor.add-link',
      mapValues(this.state.attributes, (value) =>
        value !== '' ? value : undefined
      )
    )
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      attributes: this.initialAttributes,
    })
  }

  setAttribute = (key, value) => {
    this.setState({
      attributes: {
        ...this.state.attributes,
        [key]: value,
      },
    })
  }

  render() {
    const {isOpen, t} = this.props
    const {attributes, editing} = this.state
    return (
      <Modal isOpen={isOpen} toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {editing ? t('editLink') : t('insertLink')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              this.addLinkToEditor()
            }}
          >
            <FormGroup>
              <FloatingLabelInput
                label="URL"
                name="link-url"
                onChange={(value) => this.setAttribute('href', value.trim())}
                required
                type="url"
                value={attributes.href}
              />
            </FormGroup>
            <FormGroup>
              <FloatingLabelInput
                label={t('tooltip')}
                name="link-title"
                onChange={(value) => this.setAttribute('title', value.trim())}
                value={attributes.title}
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

export default withTranslation()(LinkAdderModal)
