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

class LinkAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {
      editing: false,
      href: '',
      title: '',
    }
    subscribe('editor.show-link-modal', (_msg, data = {}) => {
      this.setState({
        ...data,
        title: data.title ?? '',
      })
      this.props.onToggle()
    })
  }

  addLinkToEditor = () => {
    const filteredState = Object.fromEntries(
      Object.entries(this.state).filter(
        ([key, value]) => key !== 'editing' && value !== ''
      )
    )
    publish('editor.add-link', filteredState)
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      href: '',
      title: '',
    })
  }

  render() {
    const {isOpen, t} = this.props
    const {editing, href, title} = this.state
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
              <Input
                onChange={(evt) =>
                  this.setState({href: evt.target.value.trim()})
                }
                placeholder="URL"
                required
                type="url"
                value={href}
              />
            </FormGroup>
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({title: evt.target.value})}
                placeholder={t('label')}
                type="text"
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

export default withTranslation()(LinkAdderModal)
