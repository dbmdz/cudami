import {publish, subscribe, unsubscribe} from 'pubsub-js'
import React from 'react'
import {Button, ButtonGroup, Card, CardBody} from 'reactstrap'
import {FaPlus, FaTrashAlt} from 'react-icons/fa'

import {getImageUrl} from './utils'

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
  if (!previewImage) {
    return (
      <Card className="rounded text-center">
        <CardBody>
          <Button
            className="stretched-link"
            color="link"
            onClick={() => {
              const token = subscribe(
                'editor.update-preview-image',
                (_msg, {previewImage, renderingHints}) => {
                  onUpdate({
                    previewImage,
                    previewImageRenderingHints: updateRenderingHints(
                      previewImageRenderingHints,
                      renderingHints,
                      language
                    ),
                  })
                  unsubscribe(token)
                }
              )
              publish('editor.show-preview-image-modal')
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
