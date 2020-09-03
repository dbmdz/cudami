import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './ImageAdderModal.css'
import ImageMetadataForm from './imageAdder/ImageMetadataForm'
import ImageRenderingHintsForm from './imageAdder/ImageRenderingHintsForm'
import ImageSelector from './imageAdder/ImageSelector'
import AppContext from '../AppContext'
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
      doUpdateRequest: false,
      fileResource: {},
      metadataOpen: true,
      renderingHintsOpen: false,
      showImageSelector: true,
      tooltipsOpen: {
        altText: false,
        caption: false,
        labelUpload: false,
        labelUrl: false,
        search: false,
        title: false,
        upload: false,
        url: false,
      },
    }
    subscribe('editor.show-image-modal', (_msg, data = {}) => {
      const attributes = Object.fromEntries(
        Object.entries(data).filter(([key, value]) => {
          // alignment is allowed to be null and should not be filtered out in that case
          if (key === 'alignment') {
            return true
          }
          return key !== 'showImageSelector' && value !== null
        })
      )
      this.setState({
        attributes: {
          ...this.initialAttributes,
          ...attributes,
        },
        fileResource: {
          ...this.state.fileResource,
          uri: attributes.url ?? '',
          uuid: attributes.resourceId,
        },
        showImageSelector: data.showImageSelector ?? true,
      })
      this.props.onToggle()
    })
  }

  async componentDidMount() {
    const newFileResource = await loadIdentifiable(
      this.context.apiContextPath,
      this.context.mockApi,
      'fileResource'
    )
    const initialFileResource = {
      ...newFileResource,
      fileResourceType: 'IMAGE',
      label: {[this.props.activeLanguage]: ''},
      mimeType: 'image/*',
      uri: '',
    }
    this.setState({
      fileResource: initialFileResource,
      initialFileResource,
    })
  }

  addImageToEditor = (resourceId) => {
    const filteredAttributes = Object.fromEntries(
      Object.entries(this.state.attributes).filter(([_, value]) => value !== '')
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
      doUpdateRequest: false,
      fileResource: this.state.initialFileResource,
      metadataOpen: true,
      renderingHintsOpen: false,
      tooltipsOpen: this.getClosedTooltipsState(),
    })
  }

  getClosedTooltipsState = () => {
    return Object.fromEntries(
      Object.entries(this.state.tooltipsOpen).map(([name, _]) => [name, false])
    )
  }

  onTabChanged = () => {
    this.setState({
      doUpdateRequest: false,
      fileResource: this.state.initialFileResource,
      tooltipsOpen: this.getClosedTooltipsState(),
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

  submitFileResource = async () => {
    const {apiContextPath, mockApi} = this.context
    let resourceId = this.state.fileResource.uuid
    if (!resourceId) {
      const {uuid} = await saveFileResource(
        apiContextPath,
        this.state.fileResource,
        mockApi
      )
      resourceId = uuid
    } else if (this.state.doUpdateRequest) {
      updateFileResource(apiContextPath, this.state.fileResource, mockApi)
    }
    return resourceId
  }

  toggleTooltip = (name) => {
    this.setState({
      tooltipsOpen: {
        ...this.getClosedTooltipsState(),
        [name]: !this.state.tooltipsOpen[name],
      },
    })
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
    const {activeLanguage, isOpen, t} = this.props
    const {
      alignment,
      altText,
      caption,
      linkNewTab,
      linkUrl,
      title,
      width,
    } = this.state.attributes
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {this.state.showImageSelector
            ? t('insert.image.new')
            : t('insert.image.edit')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const resourceId = await this.submitFileResource()
              this.addImageToEditor(resourceId)
            }}
          >
            {this.state.showImageSelector && (
              <ImageSelector
                activeLanguage={activeLanguage}
                fileResource={this.state.fileResource}
                onChange={this.updateFileResource}
                onTabChanged={this.onTabChanged}
                toggleTooltip={this.toggleTooltip}
                tooltipsOpen={this.state.tooltipsOpen}
              />
            )}
            <ImageMetadataForm
              altText={altText}
              caption={caption}
              isOpen={this.state.metadataOpen}
              onChange={this.setAttribute}
              title={title}
              toggle={() =>
                this.setState({metadataOpen: !this.state.metadataOpen})
              }
              toggleTooltip={this.toggleTooltip}
              tooltipsOpen={this.state.tooltipsOpen}
            />
            <ImageRenderingHintsForm
              alignment={alignment}
              isOpen={this.state.renderingHintsOpen}
              linkNewTab={linkNewTab}
              linkUrl={linkUrl}
              onChange={this.setAttribute}
              toggle={() =>
                this.setState({
                  renderingHintsOpen: !this.state.renderingHintsOpen,
                })
              }
              width={width}
            />
            <Button className="float-right mt-2" color="primary" type="submit">
              {t('save')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

ImageAdderModal.contextType = AppContext

export default withTranslation()(ImageAdderModal)
