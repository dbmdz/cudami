import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Label, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import ImageMetadataForm from './imageAdder/ImageMetadataForm'
import ImageRenderingHintsForm from './imageAdder/ImageRenderingHintsForm'
import ImageSelector from './imageAdder/ImageSelector'
import {loadIdentifiable, saveFileResource, updateFileResource} from '../../api'

class ImageAdderModal extends Component {
  initialAttributes = {
    alignment: 'left',
    altText: '',
    caption: '',
    linkNewTab: true,
    linkUrl: '',
    title: '',
    width: '33%',
  }

  constructor(props) {
    super(props)
    this.state = {
      attributes: this.initialAttributes,
      fileResource: {},
      metadataOpen: false,
      renderingHintsOpen: false,
      toggleEnabled: false,
    }
    subscribe('editor.show-image-modal', () => {
      this.props.onToggle()
    })
  }

  async componentDidMount() {
    const newFileResource = await loadIdentifiable(
      this.props.apiContextPath,
      'fileResource',
      'new'
    )
    const initialFileResource = {
      ...newFileResource,
      fileresourceType: 'image_fileresource',
      label: {[this.props.activeLanguage]: ''},
      mimeType: 'image/png',
      uri: '',
    }
    this.setState({
      fileResource: initialFileResource,
      initialFileResource,
    })
  }

  addImageToEditor = resourceId => {
    const filteredAttributes = Object.fromEntries(
      Object.entries(this.state.attributes).filter(([_, value]) => value)
    )
    const data = {
      ...filteredAttributes,
      resourceId,
      url: this.state.fileResource.uri,
    }
    publish('editor.add-image', data)
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      attributes: this.initialAttributes,
      fileResource: this.state.initialFileResource,
      metadataOpen: false,
      renderingHintsOpen: false,
      toggleEnabled: false,
    })
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

  submitFileResource = async () => {
    let resourceId = this.state.fileResource.uuid
    if (resourceId) {
      updateFileResource(this.props.apiContextPath, this.state.fileResource)
    } else {
      const {uuid} = await saveFileResource(
        this.props.apiContextPath,
        this.state.fileResource
      )
      resourceId = uuid
    }
    return resourceId
  }

  updateFileResource = (updateFields, additionalFields = {}) => {
    this.setState({
      fileResource: {
        ...this.state.fileResource,
        ...updateFields,
      },
      ...additionalFields,
    })
  }

  render() {
    const {apiContextPath, debug, isOpen, t} = this.props
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>{t('insert.image')}</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async evt => {
              evt.preventDefault()
              const resourceId = await this.submitFileResource()
              this.addImageToEditor(resourceId)
            }}
          >
            <ImageSelector
              apiContextPath={apiContextPath}
              fileResource={this.state.fileResource}
              onChange={(updateFields, additionalFields) =>
                this.updateFileResource(updateFields, additionalFields)
              }
            />
            <ImageMetadataForm
              attributes={this.state.attributes}
              isOpen={this.state.metadataOpen}
              onChange={(name, value) => this.setAttribute(name, value)}
              toggle={() =>
                this.setState({metadataOpen: !this.state.metadataOpen})
              }
              toggleEnabled={this.state.toggleEnabled}
            />
            <ImageRenderingHintsForm
              attributes={this.state.attributes}
              isOpen={this.state.renderingHintsOpen}
              onChange={(name, value) => this.setAttribute(name, value)}
              toggle={() =>
                this.setState({
                  renderingHintsOpen: !this.state.renderingHintsOpen,
                })
              }
              toggleEnabled={this.state.toggleEnabled}
            />
            {debug && (
              <>
                <Label className="font-weight-bold mt-3">JSON (debug)</Label>
                <pre className="border">
                  <code>{JSON.stringify(this.state, null, 4)}</code>
                </pre>
              </>
            )}
            <Button className="float-right mt-2" color="primary" type="submit">
              {t('save')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(ImageAdderModal)
