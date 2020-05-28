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
      doUpdateRequest: false,
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
    subscribe('editor.show-image-modal', () => {
      this.props.onToggle('imageAdder')
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
    this.props.onToggle('imageAdder')
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
    let resourceId = this.state.fileResource.uuid
    if (!resourceId) {
      const {uuid} = await saveFileResource(
        this.props.apiContextPath,
        this.state.fileResource
      )
      resourceId = uuid
    } else if (this.state.doUpdateRequest) {
      updateFileResource(this.props.apiContextPath, this.state.fileResource)
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
    const {
      activeLanguage,
      apiContextPath,
      debug,
      defaultLanguage,
      isOpen,
      t,
    } = this.props
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>{t('insert.image')}</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={async (evt) => {
              evt.preventDefault()
              const resourceId = await this.submitFileResource()
              this.addImageToEditor(resourceId)
            }}
          >
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
            <ImageMetadataForm
              attributes={this.state.attributes}
              isOpen={this.state.metadataOpen}
              onChange={this.setAttribute}
              toggle={() =>
                this.setState({metadataOpen: !this.state.metadataOpen})
              }
              toggleTooltip={this.toggleTooltip}
              tooltipsOpen={this.state.tooltipsOpen}
            />
            <ImageRenderingHintsForm
              attributes={this.state.attributes}
              isOpen={this.state.renderingHintsOpen}
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

export default withTranslation()(ImageAdderModal)
