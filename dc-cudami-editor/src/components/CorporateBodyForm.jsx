import React from 'react'
import {
  Card,
  CardBody,
  Col,
  Form,
  Nav,
  Row,
  TabContent,
  TabPane,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import FormEditor from './FormEditor'
import FormIdInput from './FormIdInput'
import FormButtons from './FormButtons'
import FormUrlInput from './FormUrlInput'
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import Teaser from './Teaser'

const CorporateBodyForm = ({
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
          <h1>
            {identifiable.uuid
              ? t('editCorporateBody')
              : t('createCorporateBody')}
          </h1>
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
      <Row>
        <Col sm="12">
          {identifiable.uuid && <FormIdInput id={identifiable.uuid} />}
          <FormUrlInput
            onChange={(url) => onUpdate({...identifiable, homepageUrl: url})}
            url={identifiable.homepageUrl}
          />
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
                            ...identifiable['text'],
                            [language]: document,
                          },
                        })
                      }}
                    />
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

export default CorporateBodyForm
