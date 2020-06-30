import React from 'react'
import {Card, CardBody, Col, Row} from 'reactstrap'

import FormEditor from './FormEditor'
import FormLabelInput from './FormLabelInput'
import PreviewImage from './PreviewImage'

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
            <PreviewImage
              language={language}
              previewImage={previewImage}
              previewImageRenderingHints={previewImageRenderingHints}
              onUpdate={updatePreviewImage}
            />
          </Col>
          <Col sm="10">
            <FormLabelInput
              label={label}
              language={language}
              onUpdate={(label) => {
                onUpdate('label', label)
              }}
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
