import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Label, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './ImageAdderModal.css'
import ImageMetadataForm from './imageAdder/ImageMetadataForm'
import ImageRenderingHintsForm from './imageAdder/ImageRenderingHintsForm'
import ImageSelector from './imageAdder/ImageSelector'
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
    const {activeLanguage, apiContextPath} = this.props
    const newFileResource = await loadIdentifiable(
      apiContextPath,
      'fileResource',
      'new'
    )
    const initialFileResource = {
      ...newFileResource,
      fileResourceType: 'IMAGE',
      label: {[activeLanguage]: ''},
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
    let fileResource = this.state.fileResource
    if (!fileResource.uuid) {
      fileResource = await saveFileResource(
        this.props.apiContextPath,
        this.state.fileResource
      )
    } else if (this.state.doUpdateRequest) {
      fileResource = await updateFileResource(
        this.props.apiContextPath,
        this.state.fileResource
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
    const {
      activeLanguage,
      apiContextPath,
      debug,
      defaultLanguage,
      isOpen,
      t,
    } = this.props
    const {altText, caption, linkNewTab, linkUrl, title} = this.state.attributes
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>Set preview image</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const fileResource = await this.submitFileResource()
              this.updatePreviewImage(fileResource)
            }}
          >
            {this.state.showImageSelector && (
              <ImageSelector
                activeLanguage={activeLanguage}
                apiContextPath={apiContextPath}
                defaultLanguage={defaultLanguage}
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
              isOpen={this.state.renderingHintsOpen}
              linkNewTab={linkNewTab}
              linkUrl={linkUrl}
              onChange={this.setAttribute}
              toggle={() =>
                this.setState({
                  renderingHintsOpen: !this.state.renderingHintsOpen,
                })
              }
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

export default withTranslation()(PreviewImageAdderModal)
