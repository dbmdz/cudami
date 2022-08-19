import groupBy from 'lodash-es/groupBy'
import {publish} from 'pubsub-js'
import {useTranslation} from 'react-i18next'
import {FaPlus} from 'react-icons/fa'
import {Button, Card, CardBody, Col, FormGroup, Label, Row} from 'reactstrap'
import {useContext} from 'use-context-selector'

import {toggleAllUrlAliases} from '../state/actions'
import {getShowAllUrlAliases} from '../state/selectors'
import {Context} from '../state/Store'
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
  const showAllUrlAliases = getShowAllUrlAliases()
  const {dispatch} = useContext(Context)
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
                    onClick={() =>
                      dispatch(toggleAllUrlAliases(!showAllUrlAliases))
                    }
                    size="xs"
                  >
                    {showAllUrlAliases
                      ? t('showPrimaryAlias', {
                          count: Object.keys(aliasesToRender).length,
                        })
                      : t('showAll')}
                  </Button>
                )}
                <UrlAliases
                  aliases={urlAliases}
                  aliasesToRender={aliasesToRender}
                  onUpdate={(aliases) =>
                    onUpdate('localizedUrlAliases', aliases)
                  }
                />
              </FormGroup>
              <Button
                className="align-items-center d-flex p-1"
                color="primary"
                onClick={() => publish('editor.show-add-urlaliases-dialog')}
                size="xs"
              >
                <FaPlus />
              </Button>
            </Col>
          </Row>
        )}
      </CardBody>
    </Card>
  )
}

export default Teaser
