import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {useTranslation} from 'react-i18next'
import {FaPlus, FaTrashAlt} from 'react-icons/fa'
import {Button, Card, CardBody, CardHeader, Collapse} from 'reactstrap'

import PreviewImage from '../../PreviewImage'

const handleClick = (onUpdate) => {
  const token = subscribe(
    'editor.update-preview-image',
    (_msg, {previewImage}) => {
      onUpdate(previewImage.uri, previewImage.uuid)
      unsubscribe(token)
    },
  )
  publish('editor.show-preview-image-dialog', {
    enableMetadata: false,
    enableRenderingHints: false,
  })
}

const MediaPreviewImage = ({isOpen, onUpdate, previewUrl, toggle}) => {
  const {t} = useTranslation()
  return (
    <Card className="media-adder-content">
      <CardHeader>
        <Button className="font-weight-bold p-0" color="link" onClick={toggle}>
          {t('setPreviewImage')}
        </Button>
      </CardHeader>
      <Collapse isOpen={isOpen}>
        <CardBody>
          {previewUrl ? (
            <>
              <PreviewImage
                className="d-inline-block"
                image={{uri: previewUrl}}
                width={50}
              />
              <Button
                className="align-items-center d-flex p-2"
                color="danger"
                onClick={() => onUpdate(undefined, undefined)}
                size="sm"
                title={t('removePreviewImage')}
              >
                <FaTrashAlt />
              </Button>
            </>
          ) : (
            <Button
              className="align-items-center d-flex p-2"
              color="primary"
              onClick={() => handleClick(onUpdate)}
              title={t('setPreviewImage')}
            >
              <FaPlus />
            </Button>
          )}
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default MediaPreviewImage
