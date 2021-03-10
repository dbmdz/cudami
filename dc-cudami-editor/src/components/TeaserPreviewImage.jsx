import {publish, subscribe, unsubscribe} from 'pubsub-js'
import React, {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaEdit, FaPlus, FaTrashAlt} from 'react-icons/fa'
import {Button, ButtonGroup, Card, CardBody, CardFooter} from 'reactstrap'

import FeedbackMessage from './FeedbackMessage'
import PreviewImage from './PreviewImage'

const handleClick = (
  currentPreviewImage,
  currentRenderingHints,
  language,
  onUpdate
) => {
  const token = subscribe(
    'editor.update-preview-image',
    (_msg, {previewImage, renderingHints}) => {
      onUpdate({
        ...(!currentPreviewImage && {previewImage}),
        previewImageRenderingHints: updateRenderingHints(
          currentRenderingHints,
          renderingHints,
          language
        ),
      })
      unsubscribe(token)
    }
  )
  if (currentPreviewImage && currentRenderingHints) {
    const {
      altText,
      caption,
      openLinkInNewWindow,
      targetLink,
      title,
    } = currentRenderingHints
    publish('editor.show-preview-image-modal', {
      attributes: {
        altText: altText?.[language],
        caption: caption?.[language],
        openLinkInNewWindow,
        targetLink,
        title: title?.[language],
      },
      editing: true,
      uuid: currentPreviewImage.uuid,
    })
  } else {
    publish('editor.show-preview-image-modal')
  }
}

const updateRenderingHints = (
  currentRenderingHints = {},
  {altText, caption, openLinkInNewWindow, targetLink, title},
  language
) => {
  const renderingHints = {
    altText: {
      ...currentRenderingHints.altText,
      [language]: altText,
    },
    caption: {
      ...currentRenderingHints.caption,
      [language]: caption,
    },
    openLinkInNewWindow,
    targetLink,
    title: {
      ...currentRenderingHints.title,
      [language]: title,
    },
  }
  for (let key of ['altText', 'caption', 'title']) {
    if (
      Object.values(renderingHints[key]).every((value) => value === undefined)
    ) {
      renderingHints[key] = undefined
    }
  }
  return renderingHints
}

const TeaserPreviewImage = ({
  language,
  onUpdate,
  previewImage,
  previewImageRenderingHints = {},
}) => {
  const [showRemoveNotification, setShowRemoveNotification] = useState(false)
  const {t} = useTranslation()
  if (!previewImage) {
    return (
      <Card className="rounded text-center" style={{zIndex: 0}}>
        <CardBody>
          <Button
            className="stretched-link"
            color="link"
            onClick={() => {
              handleClick(
                previewImage,
                previewImageRenderingHints,
                language,
                onUpdate
              )
            }}
            tag="a"
            title={t('setPreviewImage')}
          >
            <FaPlus />
          </Button>
        </CardBody>
        {showRemoveNotification && (
          <CardFooter className="p-0">
            <FeedbackMessage
              message={{key: 'removePreviewImageAfterSaveNotification'}}
            />
          </CardFooter>
        )}
      </Card>
    )
  }
  return (
    <Card className="rounded text-center">
      <CardBody className="p-1">
        <PreviewImage
          image={previewImage}
          language={language}
          renderingHints={previewImageRenderingHints}
          showCaption={true}
          width={200}
        />
        <ButtonGroup className="mt-1">
          <Button
            color="light"
            onClick={() => {
              handleClick(
                previewImage,
                previewImageRenderingHints,
                language,
                onUpdate
              )
            }}
            size="sm"
            title={t('editPreviewImage')}
          >
            <FaEdit />
          </Button>
          <Button
            color="light"
            onClick={() => {
              setShowRemoveNotification(true)
              onUpdate({
                previewImage: undefined,
                previewImageRenderingHints: undefined,
              })
            }}
            size="sm"
            title={t('removePreviewImage')}
          >
            <FaTrashAlt />
          </Button>
        </ButtonGroup>
      </CardBody>
    </Card>
  )
}

export default TeaserPreviewImage
