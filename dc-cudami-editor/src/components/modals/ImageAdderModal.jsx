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

class ImageAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {
      alt: '',
      src: '',
      title: '',
    }
    subscribe('editor.show-image-modal', () => {
      this.props.onToggle()
    })
  }

  addImageToEditor = () => {
    this.props.onToggle()
    publish('editor.add-image', this.state)
    this.setState({
      alt: '',
      src: '',
      title: '',
    })
  }

  render() {
    const {t} = this.props
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>
          {t('insert.image')}
        </ModalHeader>
        <ModalBody>
          <FormGroup>
            <Input
              onChange={evt => this.setState({src: evt.target.value})}
              placeholder="URL"
              required
              type="text"
              value={this.state.src}
            />
          </FormGroup>
          <FormGroup>
            <Input
              onChange={evt => this.setState({alt: evt.target.value})}
              placeholder={t('altText')}
              type="text"
              value={this.state.alt}
            />
          </FormGroup>
          <FormGroup className="mb-0">
            <Input
              onChange={evt => this.setState({title: evt.target.value})}
              placeholder={t('label')}
              type="text"
              value={this.state.title}
            />
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={this.addImageToEditor}>
            {t('add')}
          </Button>
        </ModalFooter>
      </Modal>
    )
  }
}

export default withTranslation()(ImageAdderModal)
