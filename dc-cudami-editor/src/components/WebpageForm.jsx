import React, {useState} from 'react'
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
import {useTranslation} from 'react-i18next'

import FormButtons from './FormButtons'
import FormDateInput from './FormDateInput'
import FormEditor from './FormEditor'
import FormInput from './FormInput'
import FormTemplateSelector from './FormTemplateSelector'
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import Teaser from './Teaser'
import SelectRenderingTemplateModal from './modals/SelectRenderingTemplateModal'

const WebpageForm = ({
  activeLanguage,
  canAddLanguage,
  existingLanguages,
  formId,
  identifiable,
  onAddLanguage,
  onSubmit,
  onToggleLanguage,
  onUpdate,
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const {t} = useTranslation()
  const {
    description,
    label,
    previewImage,
    previewImageRenderingHints,
    publicationEnd,
    publicationStart,
    renderingHints,
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
            <FormButtons formId={formId} />
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
              <FormInput id="uuid" label="ID" readOnly value={uuid} />
            </Col>
          </Row>
        )}
        <Row>
          <Col sm="3">
            <FormDateInput
              id="publication-start-date"
              label={t('publicationStatus.startDate')}
              onChange={(date) => onUpdate({publicationStart: date})}
              value={publicationStart}
            />
          </Col>
          <Col sm="3">
            <FormDateInput
              id="publication-end-date"
              label={t('publicationStatus.endDate')}
              onChange={(date) => onUpdate({publicationEnd: date})}
              value={publicationEnd}
            />
          </Col>
          <Col sm="3">
            <FormTemplateSelector
              onClick={() => setIsModalOpen(true)}
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
          <Col sm="12">
            <Nav tabs>
              {existingLanguages.map((language) => (
                <LanguageTab
                  activeLanguage={activeLanguage}
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
                  />
                  <Card className="border-top-0">
                    <CardBody>
                      <FormEditor
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
                      </FormEditor>
                    </CardBody>
                  </Card>
                </TabPane>
              ))}
            </TabContent>
          </Col>
        </Row>
      </Form>
      <SelectRenderingTemplateModal
        isOpen={isModalOpen}
        onSelect={(templateName) =>
          onUpdate({
            renderingHints: {
              ...renderingHints,
              templateName,
            },
          })
        }
        toggle={() => setIsModalOpen(!isModalOpen)}
      />
    </>
  )
}

export default WebpageForm
