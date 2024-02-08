import './AddMediaDialog.css'

import mapValues from 'lodash-es/mapValues'
import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {useContext, useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import {save, update} from '../../api'
import AppContext from '../AppContext'
import MediaMetadataForm from './mediaAdder/MediaMetadataForm'
import MediaPreviewImage from './mediaAdder/MediaPreviewImage'
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'

const addToEditor = (attributes, mediaType, resourceId, uri) => {
  const data = {
    ...mapValues(attributes, (value) => (value !== '' ? value : undefined)),
    resourceId,
    url: uri,
  }
  publish(`editor.add-${mediaType}`, data)
}

const submitFileResource = async (apiContextPath, fileResource, isUpdate) => {
  let resourceId = fileResource.uuid
  if (!resourceId) {
    const {uuid} = await save(apiContextPath, fileResource, 'fileResource')
    resourceId = uuid
  } else if (isUpdate) {
    update(apiContextPath, fileResource, 'fileResource')
  }
  return resourceId
}

const AddMediaDialog = ({
  activeLanguage,
  enableAltText = true,
  enableLink = true,
  enablePreviewImage = false,
  initialFileResource = {},
  isOpen,
  mediaType,
  toggle,
}) => {
  const initialFileResourceAttributes = {
    ...initialFileResource,
    fileResourceType: mediaType.toUpperCase(),
    label: {[activeLanguage]: ''},
    mimeType: `${mediaType}/*`,
    uri: '',
  }
  const initialAttributes = {
    alignment: 'left',
    caption: '',
    title: '',
    width: '33%',
  }
  if (enableAltText) {
    initialAttributes.altText = ''
  }
  if (enableLink) {
    initialAttributes.linkNewTab = true
    initialAttributes.linkUrl = ''
  }
  const initialSectionsOpen = {
    metadata: true,
    previewImage: false,
    renderingHints: false,
  }
  const {apiContextPath} = useContext(AppContext)
  const [attributes, setAttributes] = useState(initialAttributes)
  const [editing, setEditing] = useState(false)
  const [fileResource, setFileResource] = useState(
    initialFileResourceAttributes,
  )
  const [isUpdate, setIsUpdate] = useState(false)
  const [sectionsOpen, setSectionsOpen] = useState(initialSectionsOpen)
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setAttributes(initialAttributes)
    setIsUpdate(false)
    setFileResource(initialFileResourceAttributes)
    setSectionsOpen(initialSectionsOpen)
  }
  const setAttribute = (name, value) => {
    setAttributes({
      ...attributes,
      [name]: value,
    })
  }
  useEffect(() => {
    const token = subscribe(
      `editor.show-${mediaType}-dialog`,
      (_msg, {attributes: attrs = {}, editing = false} = {}) => {
        setAttributes({
          ...attributes,
          ...mapValues(attrs, (value) => value ?? ''),
        })
        setEditing(editing)
        setFileResource({
          ...fileResource,
          uri: attrs.url ?? '',
          uuid: attrs.resourceId,
        })
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  return (
    <Modal isOpen={isOpen} size="lg" toggle={destroy}>
      <ModalHeader toggle={destroy}>
        {editing
          ? t(`editor:insert.${mediaType}.edit`)
          : t(`editor:insert.${mediaType}.new`)}
      </ModalHeader>
      <ModalBody>
        <Form
          onSubmit={async (evt) => {
            evt.preventDefault()
            const resourceId = await submitFileResource(
              apiContextPath,
              fileResource,
              isUpdate,
            )
            addToEditor(attributes, mediaType, resourceId, fileResource.uri)
            destroy()
          }}
        >
          {!editing && (
            <MediaSelector
              activeLanguage={activeLanguage}
              fileResource={fileResource}
              mediaType={mediaType}
              onChange={(changedFileResource, isUpdate = false) => {
                setFileResource({...fileResource, ...changedFileResource})
                setIsUpdate(isUpdate)
              }}
              onTabChange={() => {
                setFileResource(initialFileResourceAttributes)
                setIsUpdate(false)
              }}
            />
          )}
          <MediaMetadataForm
            altText={attributes.altText}
            caption={attributes.caption}
            enableAltText={enableAltText}
            isOpen={sectionsOpen.metadata}
            mediaType={mediaType}
            onChange={setAttribute}
            title={attributes.title}
            toggle={() =>
              setSectionsOpen({
                ...sectionsOpen,
                metadata: !sectionsOpen.metadata,
              })
            }
          />
          <MediaRenderingHintsForm
            alignment={attributes.alignment}
            enableLink={enableLink}
            isOpen={sectionsOpen.renderingHints}
            linkNewTab={attributes.linkNewTab}
            linkUrl={attributes.linkUrl}
            mediaType={mediaType}
            onChange={setAttribute}
            toggle={() =>
              setSectionsOpen({
                ...sectionsOpen,
                renderingHints: !sectionsOpen.renderingHints,
              })
            }
            width={attributes.width}
          />
          {enablePreviewImage && (
            <MediaPreviewImage
              isOpen={sectionsOpen.previewImage}
              previewUrl={attributes.previewUrl}
              onUpdate={(uri, uuid) =>
                setAttributes({
                  ...attributes,
                  previewUrl: uri,
                  previewResourceId: uuid,
                })
              }
              toggle={() =>
                setSectionsOpen({
                  ...sectionsOpen,
                  previewImage: !sectionsOpen.previewImage,
                })
              }
            />
          )}
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={destroy}>
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

export default AddMediaDialog
