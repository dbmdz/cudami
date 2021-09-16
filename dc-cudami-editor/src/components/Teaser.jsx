import groupBy from 'lodash/groupBy'
import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Button, Card, CardBody, Col, FormGroup, Label, Row} from 'reactstrap'

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
  const [showAllUrlAliases, setShowAllUrlAliases] = useState(false)
  const {t} = useTranslation()
  const aliasesToRender = groupBy(urlAliases, 'website.uuid')
  const showUrlAliasesExpandButton = Object.values(aliasesToRender).some(
    (listOfAliases) => listOfAliases.length > 1,
  )
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
          <Row className="mt-3">
            <Col sm={12}>
              <FormGroup className="mb-1">
                <Label className="align-middle mb-0">{t('urlAliases')}</Label>
                {showUrlAliasesExpandButton && (
                  <Button
                    className="ml-2"
                    color="primary"
                    onClick={() => setShowAllUrlAliases(!showAllUrlAliases)}
                    size="xs"
                  >
                    {showAllUrlAliases ? t('showPrimaryAliases') : t('showAll')}
                  </Button>
                )}
                <UrlAliases
                  aliases={urlAliases}
                  aliasesToRender={aliasesToRender}
                  onUpdate={(aliases) =>
                    onUpdate('localizedUrlAliases', aliases)
                  }
                  showAll={showAllUrlAliases}
                />
              </FormGroup>
            </Col>
          </Row>
        )}
      </CardBody>
    </Card>
  )
}

export default Teaser
