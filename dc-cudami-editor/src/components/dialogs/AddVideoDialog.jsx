import './AddMediaDialog.css'

import mapValues from 'lodash/mapValues'
import {publish, subscribe} from 'pubsub-js'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import {loadIdentifiable, saveFileResource, updateFileResource} from '../../api'
import AppContext from '../AppContext'
import MediaMetadataForm from './mediaAdder/MediaMetadataForm'
import MediaPreviewImage from './mediaAdder/MediaPreviewImage'
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'

class AddVideoDialog extends Component {
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
    }
    subscribe(
      'editor.show-video-dialog',
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
      },
    )
  }

  async componentDidMount() {
    const newFileResource = await loadIdentifiable(
      this.context.apiContextPath,
      'fileResource',
    )
    const initialFileResource = {
      ...newFileResource,
      fileResourceType: 'VIDEO',
      label: {[this.props.activeLanguage]: ''},
      mimeType: 'video/*',
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
        value !== '' ? value : undefined,
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
    })
  }

  onTabChanged = () => {
    this.setState({
      doUpdateRequest: false,
      fileResource: this.state.initialFileResource,
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
    let resourceId = this.state.fileResource.uuid
    if (!resourceId) {
      const {uuid} = await saveFileResource(
        this.context.apiContextPath,
        this.state.fileResource,
      )
      resourceId = uuid
    } else if (this.state.doUpdateRequest) {
      updateFileResource(this.context.apiContextPath, this.state.fileResource)
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
    const {activeLanguage, isOpen, t} = this.props
    const {
      attributes,
      editing,
      fileResource,
      metadataOpen,
      previewImageOpen,
      renderingHintsOpen,
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
            <ButtonGroup className="float-right">
              <Button className="mr-1" color="light" onClick={this.destroy}>
                {t('cancel')}
              </Button>
              <Button color="primary" type="submit">
                {editing ? t('save') : t('add')}
              </Button>
            </ButtonGroup>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

AddVideoDialog.contextType = AppContext

export default withTranslation()(AddVideoDialog)
