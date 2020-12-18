import React from 'react'
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
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import Teaser from './Teaser'

const WebpageForm = ({
  activeLanguage,
  canAddLanguage,
  existingLanguages,
  identifiable,
  onAddLanguage,
  onSubmit,
  onToggleLanguage,
  onUpdate,
}) => {
  const {t} = useTranslation()
  return (
    <Form
      onSubmit={(evt) => {
        evt.preventDefault()
        onSubmit()
      }}
    >
      <Row>
        <Col xs="6" sm="9">
          <h1>{identifiable.uuid ? t('editWebpage') : t('createWebpage')}</h1>
        </Col>
        <Col xs="6" sm="3">
          <FormButtons />
        </Col>
      </Row>
      <Row>
        <Col sm="12">
          <hr />
        </Col>
      </Row>
      {identifiable.uuid && (
        <Row>
          <Col sm="12">
            <FormInput
              id="uuid"
              label="ID"
              readOnly
              value={identifiable.uuid}
            />
          </Col>
        </Row>
      )}
      <Row>
        <Col sm="3">
          <FormDateInput
            id="publication-start-date"
            label={t('publicationStatus.startDate')}
            onChange={(date) => onUpdate({publicationStart: date})}
            value={identifiable.publicationStart}
          />
        </Col>
        <Col sm="3">
          <FormDateInput
            id="publication-end-date"
            label={t('publicationStatus.endDate')}
            onChange={(date) => onUpdate({publicationEnd: date})}
            value={identifiable.publicationEnd}
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
                  description={identifiable.description[language]}
                  label={identifiable.label[language]}
                  language={language}
                  onUpdate={(updateKey, updateValue) =>
                    onUpdate({
                      [updateKey]: {
                        ...identifiable[updateKey],
                        [language]: updateValue,
                      },
                    })
                  }
                  previewImage={identifiable.previewImage}
                  previewImageRenderingHints={
                    identifiable.previewImageRenderingHints
                  }
                  updatePreviewImage={onUpdate}
                />
                <Card className="border-top-0">
                  <CardBody>
                    <FormEditor
                      document={identifiable.text[language]}
                      type="text"
                      onUpdate={(document) => {
                        onUpdate({
                          text: {
                            ...identifiable.text,
                            [language]: document,
                          },
                        })
                      }}
                    >
                      <CustomInput
                        checked={
                          identifiable.renderingHints.showInPageNavigation
                        }
                        className="ml-4"
                        id="show-in-page-navigation"
                        inline
                        label={t('showInPageNavigation')}
                        onChange={(evt) => {
                          onUpdate({
                            renderingHints: {
                              ...identifiable.renderingHints,
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
  )
}

export default WebpageForm
