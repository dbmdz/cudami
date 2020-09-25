import mapValues from 'lodash/mapValues'
import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {Button, Form, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import './MediaAdderModal.css'
import MediaMetadataForm from './mediaAdder/MediaMetadataForm'
import MediaPreviewImage from './mediaAdder/MediaPreviewImage'
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'
import AppContext from '../AppContext'
import {getClosedTooltipsState} from '../utils'
import {loadIdentifiable, saveFileResource, updateFileResource} from '../../api'

class VideoAdderModal extends Component {
  initialAttributes = {
    alignment: 'left',
    caption: '',
    title: '',
    width: '33%',
  }

  constructor(props) {
    super(props)
    this.state = {
      attributes: this.initialAttributes,
      doUpdateRequest: false,
      editing: false,
      fileResource: {},
      metadataOpen: true,
      previewImageOpen: false,
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
      'editor.show-video-modal',
      (_msg, {attributes = {}, editing = false} = {}) => {
        this.setState({
          attributes: {
            ...this.state.attributes,
            ...mapValues(attributes, (value) => value ?? ''),
          },
          editing,
          fileResource: {
            ...this.state.fileResource,
            uri: attributes.url ?? '',
            uuid: attributes.resourceId,
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
      fileResourceType: 'VIDEO',
      label: {[this.props.activeLanguage]: ''},
      mimeType: 'video/mp4',
      uri: '',
    }
    this.setState({
      fileResource: initialFileResource,
      initialFileResource,
    })
  }

  addVideoToEditor = (resourceId) => {
    const data = {
      ...mapValues(this.state.attributes, (value) =>
        value !== '' ? value : undefined
      ),
      resourceId,
      url: this.state.fileResource.uri,
    }
    publish('editor.add-video', data)
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

  setAttributes = (attributes) => {
    this.setState({
      attributes: {
        ...this.state.attributes,
        ...attributes,
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

  render() {
    const {activeLanguage, isOpen, t} = this.props
    const {
      attributes,
      editing,
      fileResource,
      metadataOpen,
      previewImageOpen,
      renderingHintsOpen,
      tooltipsOpen,
    } = this.state
    const mediaType = 'video'
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {editing ? t('insert.video.edit') : t('insert.video.new')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const resourceId = await this.submitFileResource()
              this.addVideoToEditor(resourceId)
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
            <MediaMetadataForm
              altText={attributes.altText}
              caption={attributes.caption}
              enableAltText={false}
              isOpen={metadataOpen}
              mediaType={mediaType}
              onChange={this.setAttribute}
              title={attributes.title}
              toggle={() => this.setState({metadataOpen: !metadataOpen})}
              toggleTooltip={this.toggleTooltip}
              tooltipsOpen={tooltipsOpen}
            />
            <MediaRenderingHintsForm
              alignment={attributes.alignment}
              enableLink={false}
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
              width={attributes.width}
            />
            <MediaPreviewImage
              isOpen={previewImageOpen}
              previewUrl={attributes.previewUrl}
              onUpdate={(uri, uuid) =>
                this.setAttributes({previewUrl: uri, previewResourceId: uuid})
              }
              toggle={() =>
                this.setState({previewImageOpen: !previewImageOpen})
              }
            />
            <Button className="float-right mt-2" color="primary" type="submit">
              {editing ? t('save') : t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

VideoAdderModal.contextType = AppContext

export default withTranslation()(VideoAdderModal)
