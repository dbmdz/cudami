import {publish, subscribe, unsubscribe} from 'pubsub-js'
import React from 'react'
import {Button, Card, CardBody, CardHeader} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaPlus, FaTrashAlt} from 'react-icons/fa'

import PreviewImage from '../../PreviewImage'

const handleClick = (onUpdate) => {
  const token = subscribe(
    'editor.update-preview-image',
    (_msg, {previewImage}) => {
      onUpdate(previewImage.uri, previewImage.uuid)
      unsubscribe(token)
    }
  )
  publish('editor.show-preview-image-modal', {
    enableMetadata: false,
    enableRenderingHints: false,
  })
}

const MediaPreviewImage = ({onUpdate, previewUrl}) => {
  const {t} = useTranslation()
  return (
    <Card className="media-adder-content">
      <CardHeader>{t('setPreviewImage')}</CardHeader>
      <CardBody>
        {previewUrl ? (
          <>
            <PreviewImage
              className="d-inline-block mb-0 mr-1"
              image={{uri: previewUrl}}
              width={50}
            />
            <Button
              color="primary"
              onClick={() => onUpdate(undefined, undefined)}
              size="sm"
              title={t('removePreviewImage')}
            >
              <FaTrashAlt />
            </Button>
          </>
        ) : (
          <Button
            color="primary"
            onClick={() => handleClick(onUpdate)}
            title={t('setPreviewImage')}
          >
            <FaPlus />
          </Button>
        )}
      </CardBody>
    </Card>
  )
}

export default MediaPreviewImage
