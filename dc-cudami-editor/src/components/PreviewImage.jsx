import {publish, subscribe, unsubscribe} from 'pubsub-js'
import React from 'react'
import {Button, ButtonGroup, Card, CardBody} from 'reactstrap'
import {FaEdit, FaPlus, FaTrashAlt} from 'react-icons/fa'

import {getImageUrl} from './utils'

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
  if (currentRenderingHints && currentPreviewImage) {
    const {
      altText,
      caption,
      openLinkInNewWindow,
      targetLink,
      title,
    } = currentRenderingHints
    publish('editor.show-preview-image-modal', {
      altText: altText[language],
      caption: caption?.[language],
      openLinkInNewWindow,
      showImageSelector: false,
      targetLink: targetLink,
      title: title?.[language],
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
  for (let key of ['caption', 'title']) {
    if (
      Object.values(renderingHints[key]).every((value) => value === undefined)
    ) {
      renderingHints[key] = undefined
    }
  }
  return renderingHints
}

const PreviewImage = ({
  language,
  onUpdate,
  previewImage,
  previewImageRenderingHints,
}) => {
  var emptyImageRenderingHints = {
    altText: {[language]: ''},
    caption: {[language]: ''},
    title: {[language]: ''},
  }
  if (previewImageRenderingHints === undefined) {
    previewImageRenderingHints = emptyImageRenderingHints
  } else {
    // get existing rendering hints from previewImageRenderingHints and
    // fill missing key/values from emptyImageRenderingHints
    previewImageRenderingHints = {
      ...emptyImageRenderingHints,
      ...previewImageRenderingHints,
    }
  }

  if (!previewImage) {
    return (
      <Card className="rounded text-center">
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
          >
            <FaPlus />
          </Button>
        </CardBody>
      </Card>
    )
  }
  const {altText, caption, title} = previewImageRenderingHints
  return (
    <Card className="rounded text-center">
      <CardBody className="p-1">
        <figure className="mb-0">
          <img
            alt={altText[language]}
            className="mw-100"
            src={getImageUrl(previewImage, '200,')}
            title={title && title[language]}
          />
          {caption && caption[language] && (
            <figcaption>{caption[language]}</figcaption>
          )}
        </figure>
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
          >
            <FaEdit />
          </Button>
          <Button
            color="light"
            onClick={() =>
              onUpdate({
                previewImage: undefined,
                previewImageRenderingHints: undefined,
              })
            }
            size="sm"
          >
            <FaTrashAlt />
          </Button>
        </ButtonGroup>
      </CardBody>
    </Card>
  )
}

export default PreviewImage
