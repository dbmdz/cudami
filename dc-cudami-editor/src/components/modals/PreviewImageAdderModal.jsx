import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './MediaAdderModal.css'
import MediaMetadataForm from './mediaAdder/MediaMetadataForm'
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'
import AppContext from '../AppContext'
import {loadIdentifiable, saveFileResource, updateFileResource} from '../../api'

class PreviewImageAdderModal extends Component {
  initialAttributes = {
    altText: '',
    caption: '',
    linkNewTab: true,
    linkUrl: '',
    title: '',
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
    subscribe('editor.show-preview-image-modal', (_msg, data = {}) => {
      const attributes = Object.fromEntries(
        Object.entries(data)
          .filter(([key, value]) => {
            return !['showImageSelector', 'uuid'].includes(key) && value
          })
          .map(([key, value]) => {
            const keyMapping = {
              openLinkInNewWindow: 'linkNewTab',
              targetLink: 'linkUrl',
            }
            if (key in keyMapping) {
              return [keyMapping[key], value]
            }
            return [key, value]
          })
      )
      this.setState({
        attributes: {
          ...this.initialAttributes,
          ...attributes,
        },
        fileResource: {
          ...this.state.fileResource,
          uuid: data.uuid,
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
      mimeType: 'image/png',
      uri: '',
    }
    this.setState({
      fileResource: initialFileResource,
      initialFileResource,
    })
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
    let fileResource = this.state.fileResource
    if (!fileResource.uuid) {
      fileResource = await saveFileResource(
        apiContextPath,
        this.state.fileResource,
        mockApi
      )
    } else if (this.state.doUpdateRequest) {
      fileResource = await updateFileResource(
        apiContextPath,
        this.state.fileResource,
        mockApi
      )
    }
    return fileResource
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

  updatePreviewImage = (fileResource) => {
    const filteredAttributes = Object.fromEntries(
      Object.entries(this.state.attributes).filter(([_, value]) => value !== '')
    )
    const {altText, caption, linkNewTab, linkUrl, title} = filteredAttributes
    publish('editor.update-preview-image', {
      previewImage: {
        ...fileResource,
        fileResourceType: 'IMAGE',
      },
      renderingHints: {
        altText,
        caption,
        openLinkInNewWindow: linkNewTab,
        targetLink: linkUrl,
        title,
      },
    })
    this.destroy()
  }

  render() {
    const {activeLanguage, isOpen, t} = this.props
    const {altText, caption, linkNewTab, linkUrl, title} = this.state.attributes
    const mediaType = 'image'
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {this.state.showImageSelector
            ? t('setPreviewImage')
            : t('editPreviewImage')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const fileResource = await this.submitFileResource()
              this.updatePreviewImage(fileResource)
            }}
          >
            {this.state.showImageSelector && (
              <MediaSelector
                activeLanguage={activeLanguage}
                fileResource={this.state.fileResource}
                mediaType={mediaType}
                onChange={this.updateFileResource}
                onTabChanged={this.onTabChanged}
                toggleTooltip={this.toggleTooltip}
                tooltipsOpen={this.state.tooltipsOpen}
              />
            )}
            <MediaMetadataForm
              altText={altText}
              caption={caption}
              isOpen={this.state.metadataOpen}
              mediaType={mediaType}
              onChange={this.setAttribute}
              title={title}
              toggle={() =>
                this.setState({metadataOpen: !this.state.metadataOpen})
              }
              toggleTooltip={this.toggleTooltip}
              tooltipsOpen={this.state.tooltipsOpen}
            />
            <MediaRenderingHintsForm
              enableAlignment={false}
              enableWidth={false}
              isOpen={this.state.renderingHintsOpen}
              linkNewTab={linkNewTab}
              linkUrl={linkUrl}
              mediaType={mediaType}
              onChange={this.setAttribute}
              toggle={() =>
                this.setState({
                  renderingHintsOpen: !this.state.renderingHintsOpen,
                })
              }
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

PreviewImageAdderModal.contextType = AppContext

export default withTranslation()(PreviewImageAdderModal)
