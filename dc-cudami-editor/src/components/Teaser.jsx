import React from 'react'
import {Card, CardBody, Col, Row} from 'reactstrap'

import FormEditor from './FormEditor'
import FormInput from './FormInput'
import TeaserPreviewImage from './TeaserPreviewImage'

const Teaser = ({
  description,
  label,
  language,
  onUpdate,
  previewImage,
  previewImageRenderingHints,
  updatePreviewImage,
}) => {
  return (
    <Card className="bg-light">
      <CardBody>
        <Row>
          <Col sm="2">
            <TeaserPreviewImage
              language={language}
              previewImage={previewImage}
              previewImageRenderingHints={previewImageRenderingHints}
              onUpdate={updatePreviewImage}
            />
          </Col>
          <Col sm="10">
            <FormInput
              id={`label-${language}`}
              labelKey="label"
              onChange={(label) => {
                onUpdate('label', label)
              }}
              value={label}
            />
            <FormEditor
              document={description}
              onUpdate={(document) => {
                onUpdate('description', document)
              }}
              restrictedMenu={true}
              type="description"
            />
          </Col>
        </Row>
      </CardBody>
    </Card>
  )
}

export default Teaser
