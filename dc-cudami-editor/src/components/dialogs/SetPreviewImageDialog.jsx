import './AddMediaDialog.css'

import transform from 'lodash-es/transform'
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
import MediaRenderingHintsForm from './mediaAdder/MediaRenderingHintsForm'
import MediaSelector from './mediaAdder/MediaSelector'

const submitFileResource = async (apiContextPath, fileResource, isUpdate) => {
  if (!fileResource.uuid) {
    fileResource = await save(apiContextPath, fileResource, 'fileResource')
  } else if (isUpdate) {
    fileResource = update(apiContextPath, fileResource, 'fileResource')
  }
  return fileResource
}

const updatePreviewImage = (attributes, fileResource, destroy) => {
  publish('editor.update-preview-image', {
    previewImage: {
      ...fileResource,
      fileResourceType: 'IMAGE',
    },
    renderingHints: transform(attributes, (result, value, key) => {
      const keyMapping = {
        linkNewTab: 'openLinkInNewWindow',
        linkUrl: 'targetLink',
      }
      result[keyMapping[key] ?? key] = value !== '' ? value : undefined
    }),
  })
  destroy()
}

const SetPreviewImageDialog = ({
  activeLanguage,
  initialFileResource = {},
  isOpen,
  toggle,
}) => {
  const initialFileResourceAttributes = {
    ...initialFileResource,
    fileResourceType: 'IMAGE',
    label: {[activeLanguage]: ''},
    mimeType: 'image/*',
    uri: '',
  }
  const initialAttributes = {
    altText: '',
    caption: '',
    linkNewTab: true,
    linkUrl: '',
    title: '',
  }
  const initialSectionsOpen = {
    metadata: true,
    renderingHints: false,
  }
  const {apiContextPath} = useContext(AppContext)
  const [attributes, setAttributes] = useState(initialAttributes)
  const [editing, setEditing] = useState(false)
  const [fileResource, setFileResource] = useState(
    initialFileResourceAttributes,
  )
  const [isUpdate, setIsUpdate] = useState(false)
  const [sectionsEnabled, setSectionsEnabled] = useState({
    metadata: true,
    renderingHints: true,
  })
  const [sectionsOpen, setSectionsOpen] = useState(initialSectionsOpen)
  const {t} = useTranslation()
  const mediaType = 'image'
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
      'editor.show-preview-image-dialog',
      (
        _msg,
        {attributes: attrs = {}, editing = false, sections, uuid} = {},
      ) => {
        setAttributes({
          ...attributes,
          ...transform(attrs, (result, value, key) => {
            const keyMapping = {
              openLinkInNewWindow: 'linkNewTab',
              targetLink: 'linkUrl',
            }
            result[keyMapping[key] ?? key] = value ?? ''
          }),
        })
        setEditing(editing)
        setFileResource({
          ...fileResource,
          uuid,
        })
        setSectionsEnabled({
          ...sectionsEnabled,
          ...sections,
        })
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  return (
    <Modal isOpen={isOpen} size="lg" toggle={destroy}>
      <ModalHeader toggle={destroy}>
        {editing ? t('editPreviewImage') : t('setPreviewImage')}
      </ModalHeader>
      <ModalBody>
        <Form
          onSubmit={async (evt) => {
            evt.preventDefault()
            const resource = await submitFileResource(
              apiContextPath,
              fileResource,
              isUpdate,
            )
            updatePreviewImage(attributes, resource, destroy)
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
          {sectionsEnabled.metadata && (
            <MediaMetadataForm
              altText={attributes.altText}
              caption={attributes.caption}
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
          )}
          {sectionsEnabled.renderingHints && (
            <MediaRenderingHintsForm
              enableAlignment={false}
              enableWidth={false}
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
            />
          )}
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={destroy}>
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

export default SetPreviewImageDialog
