import transform from 'lodash/transform'
import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './MediaAdderModal.css'
import MediaMetadataForm from './mediaAdder/MediaMetadataForm'
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'
import AppContext from '../AppContext'
import {getClosedTooltipsState} from '../utils'
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
      editing: false,
      enableMetadata: true,
      enableRenderingHints: true,
      fileResource: {},
      metadataOpen: true,
      renderingHintsOpen: false,
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
    subscribe(
      'editor.show-preview-image-modal',
      (
        _msg,
        {
          attributes = {},
          editing = false,
          enableMetadata = true,
          enableRenderingHints = true,
          uuid,
        } = {}
      ) => {
        this.setState({
          attributes: {
            ...this.state.attributes,
            ...transform(attributes, (result, value, key) => {
              const keyMapping = {
                openLinkInNewWindow: 'linkNewTab',
                targetLink: 'linkUrl',
              }
              result[keyMapping[key] ?? key] = value ?? ''
            }),
          },
          editing,
          enableMetadata,
          enableRenderingHints,
          fileResource: {
            ...this.state.fileResource,
            uuid,
          },
        })
        this.props.onToggle()
      }
    )
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
      tooltipsOpen: getClosedTooltipsState(this.state.tooltipsOpen),
    })
  }

  onTabChanged = () => {
    this.setState({
      doUpdateRequest: false,
      fileResource: this.state.initialFileResource,
      tooltipsOpen: getClosedTooltipsState(this.state.tooltipsOpen),
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
        ...getClosedTooltipsState(this.state.tooltipsOpen),
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
    publish('editor.update-preview-image', {
      previewImage: {
        ...fileResource,
        fileResourceType: 'IMAGE',
      },
      renderingHints: transform(this.state.attributes, (result, value, key) => {
        const keyMapping = {
          linkNewTab: 'openLinkInNewWindow',
          linkUrl: 'targetLink',
        }
        result[keyMapping[key] ?? key] = value !== '' ? value : undefined
      }),
    })
    this.destroy()
  }

  render() {
    const {activeLanguage, isOpen, t} = this.props
    const {
      attributes,
      editing,
      enableMetadata,
      enableRenderingHints,
      fileResource,
      metadataOpen,
      renderingHintsOpen,
      tooltipsOpen,
    } = this.state
    const mediaType = 'image'
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {editing ? t('editPreviewImage') : t('setPreviewImage')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const fileResource = await this.submitFileResource()
              this.updatePreviewImage(fileResource)
            }}
          >
            {!editing && (
              <MediaSelector
                activeLanguage={activeLanguage}
                fileResource={fileResource}
                mediaType={mediaType}
                onChange={this.updateFileResource}
                onTabChanged={this.onTabChanged}
                toggleTooltip={this.toggleTooltip}
                tooltipsOpen={tooltipsOpen}
              />
            )}
            {enableMetadata && (
              <MediaMetadataForm
                altText={attributes.altText}
                caption={attributes.caption}
                isOpen={metadataOpen}
                mediaType={mediaType}
                onChange={this.setAttribute}
                title={attributes.title}
                toggle={() => this.setState({metadataOpen: !metadataOpen})}
                toggleTooltip={this.toggleTooltip}
                tooltipsOpen={tooltipsOpen}
              />
            )}
            {enableRenderingHints && (
              <MediaRenderingHintsForm
                enableAlignment={false}
                enableWidth={false}
                isOpen={renderingHintsOpen}
                linkNewTab={attributes.linkNewTab}
                linkUrl={attributes.linkUrl}
                mediaType={mediaType}
                onChange={this.setAttribute}
                toggle={() =>
                  this.setState({
                    renderingHintsOpen: !renderingHintsOpen,
                  })
                }
              />
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

PreviewImageAdderModal.contextType = AppContext

export default withTranslation()(PreviewImageAdderModal)
