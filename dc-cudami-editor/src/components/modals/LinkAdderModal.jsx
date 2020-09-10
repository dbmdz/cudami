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
      href: '',
      title: '',
    }
    subscribe('editor.show-link-modal', () => {
      this.props.onToggle()
    })
  }

  addLinkToEditor = () => {
    publish('editor.add-link', this.state)
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
    return (
      <Modal isOpen={isOpen} toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>{t('insertLink')}</ModalHeader>
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
                value={this.state.href}
              />
            </FormGroup>
            <FormGroup>
              <Input
                onChange={(evt) => this.setState({title: evt.target.value})}
                placeholder={t('label')}
                type="text"
                value={this.state.height}
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

export default withTranslation()(LinkAdderModal)
