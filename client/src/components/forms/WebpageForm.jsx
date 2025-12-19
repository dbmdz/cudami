import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Card,
  CardBody,
  Col,
  CustomInput,
  Form,
  Nav,
  Row,
  TabContent,
  TabPane,
} from 'reactstrap'

import DateInput from '../DateInput'
import SelectRenderingTemplateDialog from '../dialogs/SelectRenderingTemplateDialog'
import EditorWithLabel from '../editor/EditorWithLabel'
import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import Teaser from '../Teaser'
import TemplateSelector from '../TemplateSelector'
import ActionButtons from './ActionButtons'

const WebpageForm = ({
  activeLanguage,
  canAddLanguage,
  enableUrlAliases,
  existingLanguages,
  formId,
  identifiable,
  invalidLanguages,
  onAddLanguage,
  onSubmit,
  onToggleLanguage,
  onUpdate,
}) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const {t} = useTranslation()
  const {
    externalUrl,
    description,
    label,
    previewImage,
    previewImageRenderingHints,
    publicationEnd,
    publicationStart,
    renderingHints,
    showExternalAsInternalUrl = false,
    text,
    uuid,
  } = identifiable
  return (
    <>
      <Form
        id={formId}
        onSubmit={(evt) => {
          evt.preventDefault()
          onSubmit()
        }}
      >
        <Row>
          <Col xs="6" sm="9">
            <h1>{uuid ? t('editWebpage') : t('createWebpage')}</h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons
              disabled={invalidLanguages.length > 0}
              formId={formId}
            />
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            <hr />
          </Col>
        </Row>
        {uuid && (
          <Row>
            <Col sm="12">
              <InputWithLabel id="uuid" label="ID" readOnly value={uuid} />
            </Col>
          </Row>
        )}
        <Row>
          <Col md="3" sm="6">
            <DateInput
              id="publication-start-date"
              label={t('publicationStatus:startDate')}
              onChange={(date) => onUpdate({publicationStart: date})}
              value={publicationStart}
            />
          </Col>
          <Col md="3" sm="6">
            <DateInput
              id="publication-end-date"
              label={t('publicationStatus:endDate')}
              onChange={(date) => onUpdate({publicationEnd: date})}
              value={publicationEnd}
            />
          </Col>
          <Col sm="3">
            <TemplateSelector
              onClick={() => setIsDialogOpen(true)}
              onRemove={() =>
                onUpdate({
                  renderingHints: {
                    ...renderingHints,
                    templateName: undefined,
                  },
                })
              }
              templateName={renderingHints.templateName}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <InputWithLabel
              id="external-url"
              labelKey="externalUrl"
              type="url"
              value={externalUrl}
              onChange={(externalUrl) => onUpdate({externalUrl})}
            />
          </Col>
          <Col className="align-self-center">
            <CustomInput
              id="show-external-url-as-internal"
              type="switch"
              label={t('showExternalAsInternal')}
              className="mt-2"
              checked={showExternalAsInternalUrl}
              onChange={(event) =>
                onUpdate({
                  showExternalAsInternalUrl: event.target.checked,
                })
              }
            />
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            <Nav tabs>
              {existingLanguages.map((language) => (
                <LanguageTab
                  activeLanguage={activeLanguage}
                  enableRemove={existingLanguages.length > 1}
                  invalid={invalidLanguages.includes(language)}
                  key={language}
                  language={language}
                  toggle={onToggleLanguage}
                />
              ))}
              {canAddLanguage && <LanguageAdder onClick={onAddLanguage} />}
            </Nav>
            <TabContent activeTab={activeLanguage}>
              {existingLanguages.map((language) => (
                /* TODO: extract as component */
                <TabPane key={language} tabId={language}>
                  <Teaser
                    description={description[language]}
                    enableUrlAliases={enableUrlAliases}
                    label={label[language]}
                    language={language}
                    onUpdate={(updateKey, updateValue) =>
                      onUpdate({
                        [updateKey]: {
                          ...identifiable[updateKey],
                          [language]: updateValue,
                        },
                      })
                    }
                    previewImage={previewImage}
                    previewImageRenderingHints={previewImageRenderingHints}
                    updatePreviewImage={onUpdate}
                    urlAliases={identifiable.localizedUrlAliases?.[language]}
                  />
                  <Card className="border-top-0">
                    <CardBody>
                      <EditorWithLabel
                        document={text[language]}
                        type="text"
                        onUpdate={(document) => {
                          onUpdate({
                            text: {
                              ...text,
                              [language]: document,
                            },
                          })
                        }}
                      >
                        <CustomInput
                          checked={renderingHints.showInPageNavigation}
                          className="ml-4"
                          id="show-in-page-navigation"
                          inline
                          label={t('showInPageNavigation')}
                          onChange={(evt) => {
                            onUpdate({
                              renderingHints: {
                                ...renderingHints,
                                showInPageNavigation: evt.target.checked,
                              },
                            })
                          }}
                          type="switch"
                        />
                      </EditorWithLabel>
                    </CardBody>
                  </Card>
                </TabPane>
              ))}
            </TabContent>
          </Col>
        </Row>
      </Form>
      <SelectRenderingTemplateDialog
        isOpen={isDialogOpen}
        onSelect={(templateName) =>
          onUpdate({
            renderingHints: {
              ...renderingHints,
              templateName,
            },
          })
        }
        toggle={() => setIsDialogOpen(!isDialogOpen)}
      />
    </>
  )
}

export default WebpageForm
