import React from 'react'
import {Button, ButtonGroup, Col, ListGroupItem, Row} from 'reactstrap'
import {FaEye, FaTrash} from 'react-icons/fa'

import {getImageUrl} from './utils'

const IdentifiableListItem = ({
  apiContextPath,
  index,
  label,
  lastModified,
  onRemove,
  previewImage,
  previewImageRenderingHints = {},
  type,
  uiLocale,
  uuid,
}) => {
  const {altText, title, caption} = previewImageRenderingHints
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
              alt={altText?.[uiLocale]}
              className="mw-100"
              src={
                previewImage
                  ? getImageUrl(previewImage, '30,')
                  : `${apiContextPath}images/no-image.png`
              }
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
            <Button className="p-0" color="link" href={viewUrl}>
              <FaEye />
            </Button>
            <Button className="p-0 ml-1" color="link" onClick={onRemove}>
              <FaTrash />
            </Button>
          </ButtonGroup>
        </Col>
      </Row>
    </ListGroupItem>
  )
}

export default IdentifiableListItem
