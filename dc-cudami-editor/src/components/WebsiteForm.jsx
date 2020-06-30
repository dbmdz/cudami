import React from 'react'
import {Col, Form, Nav, Row, TabContent, TabPane} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import FormIdInput from './FormIdInput'
import FormUrlInput from './FormUrlInput'
import FormButtons from './FormButtons'
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import Teaser from './Teaser'

const WebsiteForm = ({
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
              ? t('editWebsite', {url: identifiable.url})
              : t('createWebsite')}
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
            onChange={(url) => onUpdate({...identifiable, url})}
            url={identifiable.url}
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
              </TabPane>
            ))}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
}

export default WebsiteForm
