import {Card, CardBody, Col, Row} from 'reactstrap'

import EditorWithLabel from './editor/EditorWithLabel'
import InputWithLabel from './InputWithLabel'
import TeaserPreviewImage from './TeaserPreviewImage'
import UrlAliases from './UrlAliases'

const Teaser = ({
  description,
  enableUrlAliases = true,
  label,
  language,
  onUpdate,
  previewImage,
  previewImageRenderingHints,
  updatePreviewImage,
  urlAliases,
}) => {
  return (
    <Card className="bg-light mb-0">
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
            <InputWithLabel
              id={`label-${language}`}
              labelKey="label"
              onChange={(label) => {
                onUpdate('label', label)
              }}
              required
              value={label}
            />
            <EditorWithLabel
              document={description}
              onUpdate={(document) => {
                onUpdate('description', document)
              }}
              restrictedMenu={true}
              type="description"
            />
          </Col>
        </Row>
        {enableUrlAliases && (
          <UrlAliases
            aliases={urlAliases}
            onUpdate={(aliases) => onUpdate('localizedUrlAliases', aliases)}
          />
        )}
      </CardBody>
    </Card>
  )
}

export default Teaser
