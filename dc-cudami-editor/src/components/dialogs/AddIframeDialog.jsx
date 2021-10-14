import mapValues from 'lodash/mapValues'
import {publish, subscribe} from 'pubsub-js'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  FormText,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import InputWithFloatingLabel from '../InputWithFloatingLabel'

class AddIframeDialog extends Component {
  initialAttributes = {
    height: '',
    src: '',
    title: '',
    width: '',
  }

  constructor(props) {
    super(props)
    this.state = {
      attributes: this.initialAttributes,
      editing: false,
    }
    subscribe(
      'editor.show-iframe-dialog',
      (_msg, {attributes = {}, editing = false} = {}) => {
        this.setState({
          attributes: {
            ...this.state.attributes,
            ...mapValues(attributes, (value) => value ?? ''),
          },
          editing,
        })
        this.props.onToggle()
      },
    )
  }

  addIframeToEditor = () => {
    publish(
      'editor.add-iframe',
      mapValues(this.state.attributes, (value) =>
        value !== '' ? value : undefined,
      ),
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
              <InputWithFloatingLabel
                label="URL"
                name="iframe-url"
                onChange={(value) => this.setAttribute('src', value.trim())}
                required
                type="url"
                value={attributes.src}
              />
            </FormGroup>
            <FormGroup>
              <InputWithFloatingLabel
                label={t('width')}
                name="iframe-width"
                onChange={(value) => this.setAttribute('width', value)}
                value={attributes.width}
              />
              <FormText className="ml-1">
                {t('forExample')} <code>300px</code> or <code>50%</code>
              </FormText>
            </FormGroup>
            <FormGroup>
              <InputWithFloatingLabel
                label={t('height')}
                name="iframe-height"
                onChange={(value) => this.setAttribute('height', value)}
                value={attributes.height}
              />
              <FormText className="ml-1">
                {t('forExample')} <code>300px</code> or <code>50%</code>
              </FormText>
            </FormGroup>
            <FormGroup>
              <InputWithFloatingLabel
                label={t('tooltip')}
                name="iframe-title"
                onChange={(value) => this.setAttribute('title', value)}
                value={attributes.title}
              />
            </FormGroup>
            <ButtonGroup className="float-right">
              <Button className="mr-1" color="light" onClick={this.destroy}>
                {t('cancel')}
              </Button>
              <Button color="primary" type="submit">
                {editing ? t('save') : t('add')}
              </Button>
            </ButtonGroup>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(AddIframeDialog)
