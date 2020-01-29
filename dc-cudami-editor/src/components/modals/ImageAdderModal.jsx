import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import {faCode, faUpload} from '@fortawesome/free-solid-svg-icons'
import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  ListGroup,
  ListGroupItem,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import ImageMetadataForm from './imageAdder/ImageMetadataForm'
import ImageRenderingHintsForm from './imageAdder/ImageRenderingHintsForm'
import ImageUploadForm from './imageAdder/ImageUploadForm'
import ImageUrlForm from './imageAdder/ImageUrlForm'

class ImageAdderModal extends Component {
  constructor(props) {
    super(props)
    this.initialAttributes = {
      alignment: 'left',
      altText: '',
      caption: '',
      label: '',
      linkNewTab: true,
      linkUrl: '',
      title: '',
      url: '',
      width: '33%',
    }
    this.state = {
      attributes: this.initialAttributes,
      readOnly: false,
      step: 'initial',
    }
    subscribe('editor.show-image-modal', () => {
      this.props.onToggle()
    })
  }

  addImageToEditor = () => {
    this.toggle()
    const filteredAttributes = Object.entries(this.state.attributes).reduce(
      (filteredAttributes, attribute) => {
        if (attribute[1]) {
          filteredAttributes[attribute[0]] = attribute[1]
        }
        return filteredAttributes
      },
      {}
    )
    publish('editor.add-image', filteredAttributes)
  }

  getStepComponent = t => {
    const STEP_COMPONENT_MAPPING = {
      initial: (
        <ListGroup>
          <ListGroupItem
            className="text-decoration-none"
            href="#"
            onClick={() => this.setState({step: 'url'})}
            tag="a"
          >
            <FontAwesomeIcon icon={faCode} /> URL
          </ListGroupItem>
          <ListGroupItem
            className="text-decoration-none"
            href="#"
            onClick={() => this.setState({step: 'upload'})}
            tag="a"
          >
            <FontAwesomeIcon icon={faUpload} /> Upload
          </ListGroupItem>
        </ListGroup>
      ),
      metadata: (
        <ImageMetadataForm
          attributes={this.state.attributes}
          onChange={(name, value) => this.setAttribute(name, value)}
          onSubmit={() => this.setState({step: 'renderingHints'})}
        />
      ),
      renderingHints: (
        <ImageRenderingHintsForm
          attributes={this.state.attributes}
          onChange={(name, value) => this.setAttribute(name, value)}
          onSubmit={() => this.addImageToEditor()}
        />
      ),
      upload: (
        <ImageUploadForm
          apiContextPath={this.props.apiContextPath}
          onChange={attributes => this.setAttributes(attributes)}
          onSubmit={() => this.setState({readOnly: true, step: 'url'})}
        />
      ),
      url: (
        <ImageUrlForm
          attributes={this.state.attributes}
          onChange={(name, value) => this.setAttribute(name, value)}
          onSubmit={() => this.setState({step: 'metadata'})}
          readOnly={this.state.readOnly}
        />
      ),
    }
    return STEP_COMPONENT_MAPPING[this.state.step]
  }

  getStepTitle = t => {
    const STEP_TITLE_MAPPING = {
      initial: 'Bild einfügen über',
      renderingHints: 'Darstellung',
      upload: 'Hochladen',
      url: 'URL eingeben',
      metadata: 'Metadaten eingeben',
    }
    return STEP_TITLE_MAPPING[this.state.step]
  }

  setAttribute = (name, value) => {
    this.setState({
      attributes: {
        ...this.state.attributes,
        [name]: value,
      },
    })
  }

  setAttributes = attributes => {
    this.setState({
      attributes: {
        ...this.state.attributes,
        ...attributes,
      },
    })
  }

  toggle = () => {
    this.props.onToggle()
    this.setState({
      attributes: this.initialAttributes,
      readOnly: false,
      step: 'initial',
    })
  }

  render() {
    const {t} = this.props
    return (
      <Modal isOpen={this.props.isOpen} size="xl" toggle={this.toggle}>
        <ModalHeader toggle={this.toggle}>{this.getStepTitle(t)}</ModalHeader>
        <ModalBody>{this.getStepComponent(t)}</ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(ImageAdderModal)
