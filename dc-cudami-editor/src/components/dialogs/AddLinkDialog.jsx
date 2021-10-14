import mapValues from 'lodash/mapValues'
import {publish, subscribe} from 'pubsub-js'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import InputWithFloatingLabel from '../InputWithFloatingLabel'

class AddLinkDialog extends Component {
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
      'editor.show-link-dialog',
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

  addLinkToEditor = () => {
    publish(
      'editor.add-link',
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
              <InputWithFloatingLabel
                label="URL"
                name="link-url"
                onChange={(value) => this.setAttribute('href', value.trim())}
                pattern="^(https?://|/|mailto:).*$"
                required
                value={attributes.href}
              />
            </FormGroup>
            <FormGroup>
              <InputWithFloatingLabel
                label={t('tooltip')}
                name="link-title"
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

export default withTranslation()(AddLinkDialog)
