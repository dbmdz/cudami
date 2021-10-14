import './AddMediaDialog.css'

import transform from 'lodash/transform'
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
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'

class AddPreviewImageDialog extends Component {
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
    }
    subscribe(
      'editor.show-preview-image-dialog',
      (
        _msg,
        {
          attributes = {},
          editing = false,
          enableMetadata = true,
          enableRenderingHints = true,
          uuid,
        } = {},
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

  submitFileResource = async () => {
    let fileResource = this.state.fileResource
    if (!fileResource.uuid) {
      fileResource = await saveFileResource(
        this.context.apiContextPath,
        this.state.fileResource,
      )
    } else if (this.state.doUpdateRequest) {
      fileResource = await updateFileResource(
        this.context.apiContextPath,
        this.state.fileResource,
      )
    }
    return fileResource
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
            <ButtonGroup className="float-right">
              <Button className="mr-1" color="light" onClick={this.destroy}>
                {t('cancel')}
              </Button>
              <Button color="primary" type="submit">
                {t('save')}
              </Button>
            </ButtonGroup>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

AddPreviewImageDialog.contextType = AppContext

export default withTranslation()(AddPreviewImageDialog)
