import React from 'react'
import {Col, Form, Nav, Row, TabContent} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import FormEditor from './FormEditor'
import FormIdInput from './FormIdInput'
import FormButtons from './FormButtons'
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import LanguageTabContent from './LanguageTabContent'

const ProjectForm = ({
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
          <h1>{identifiable.uuid ? t('editProject') : t('createProject')}</h1>
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
          <Nav tabs>
            {existingLanguages.map((language) => (
              <LanguageTab
                activeLanguage={activeLanguage}
                key={language}
                language={language}
                onClick={(language) => onToggleLanguage(language)}
              />
            ))}
            {canAddLanguage && <LanguageAdder onClick={onAddLanguage} />}
          </Nav>
          <TabContent activeTab={activeLanguage}>
            {existingLanguages.map((language) => (
              <LanguageTabContent
                description={identifiable.description[language]}
                key={language}
                label={identifiable.label[language]}
                language={language}
                onUpdate={(updateKey, updateValue) =>
                  onUpdate({
                    ...identifiable,
                    [updateKey]: {
                      ...identifiable[updateKey],
                      [language]: updateValue,
                    },
                  })
                }
              >
                <FormEditor
                  document={identifiable.text[language]}
                  type="text"
                  onUpdate={(document) => {
                    onUpdate({
                      ...identifiable,
                      text: {
                        ...identifiable['text'],
                        [language]: document,
                      },
                    })
                  }}
                />
              </LanguageTabContent>
            ))}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
}

export default ProjectForm
