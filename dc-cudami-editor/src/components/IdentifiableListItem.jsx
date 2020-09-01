import React from 'react'
import {Button, ButtonGroup, Col, ListGroupItem, Row} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaExchangeAlt, FaEye, FaPencilAlt, FaUnlink} from 'react-icons/fa'

import {getImageUrl} from './utils'

const IdentifiableListItem = ({
  apiContextPath,
  enableMove,
  enableRemove,
  index,
  label,
  lastModified,
  onMove,
  onRemove,
  parentType,
  previewImage,
  previewImageRenderingHints = {},
  showEdit,
  type,
  uiLocale,
  uuid,
}) => {
  const {altText, title, caption} = previewImageRenderingHints
  const previewImageWidth = 30
  const {t} = useTranslation()
  const viewUrl = `${apiContextPath}${type.toLowerCase()}s/${uuid}`
  return (
    <ListGroupItem className="list-item pb-0 pt-0">
      <Row>
        <Col className="border-right pb-1 pt-1 text-right" md="1">
          {index}
        </Col>
        <Col className="border-right pb-1 pt-1 text-center" md="1">
          <figure className="mb-0">
            <img
              alt={altText?.[uiLocale] ?? ''}
              src={
                previewImage
                  ? getImageUrl(previewImage, `${previewImageWidth},`)
                  : `${apiContextPath}images/no-image.png`
              }
              style={{maxWidth: `${previewImageWidth}px`}}
              title={title?.[uiLocale]}
            />
            {caption?.[uiLocale] && (
              <figcaption>{caption[uiLocale]}</figcaption>
            )}
          </figure>
        </Col>
        <Col md="7" className="border-right pb-1 pt-1">
          <a href={viewUrl}>{label}</a>
        </Col>
        <Col className="border-right pb-1 pt-1 text-center" md="2">
          {new Date(lastModified).toLocaleString(uiLocale, {hour12: false})}
        </Col>
        <Col className="pb-1 pt-1 text-center" md="1">
          <ButtonGroup>
            <Button
              className="p-0"
              color="link"
              href={viewUrl}
              title={t('view')}
            >
              <FaEye />
            </Button>
            {showEdit && (
              <Button
                className="ml-1 p-0"
                color="link"
                href={`${viewUrl}/edit`}
                title={t('edit')}
              >
                <FaPencilAlt />
              </Button>
            )}
          </ButtonGroup>
          <ButtonGroup>
            {enableMove && (
              <Button
                className="ml-1 p-0"
                color="link"
                onClick={onMove}
                title={t(`moveTo.${parentType}`)}
              >
                <FaExchangeAlt />
              </Button>
            )}
            {enableRemove && (
              <Button
                className="ml-1 p-0"
                color="link"
                onClick={onRemove}
                title={t(`removeFrom.${parentType}`)}
              >
                <FaUnlink />
              </Button>
            )}
          </ButtonGroup>
        </Col>
      </Row>
    </ListGroupItem>
  )
}

export default IdentifiableListItem
