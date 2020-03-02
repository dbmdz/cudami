import React from 'react'
import {Col, Form, Nav, Row, TabContent} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import './FileResourceForm.css'
import FileResourceUploadForm from './FileResourceUploadForm'
import FormIdInput from './FormIdInput'
import FormButtons from './FormButtons'
import LanguageAdder from './LanguageAdder'
import LanguageTab from './LanguageTab'
import LanguageTabContent from './LanguageTabContent'

const FileResourceForm = props => {
  const {t} = useTranslation()
  if (!props.identifiable.uuid) {
    return (
      <FileResourceUploadForm
        apiContextPath={props.apiContextPath}
        onUpdate={props.onUpdate}
      />
    )
  }
  return (
    <Form
      onSubmit={evt => {
        evt.preventDefault()
        props.onSubmit()
      }}
    >
      <Row>
        <Col xs="6" sm="9">
          <h1>{t('editFileResource', {name: props.identifiable.filename})}</h1>
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
          <FormIdInput id={props.identifiable.uuid} />
          <Nav tabs>
            {props.existingLanguages.map(language => (
              <LanguageTab
                activeLanguage={props.activeLanguage}
                key={language}
                language={language}
                onClick={language => props.onToggleLanguage(language)}
              />
            ))}
            {props.canAddLanguage && (
              <LanguageAdder onClick={props.onAddLanguage} />
            )}
          </Nav>
          <TabContent activeTab={props.activeLanguage}>
            {props.existingLanguages.map(language => (
              <LanguageTabContent
                description={props.identifiable.description[language]}
                key={language}
                label={props.identifiable.label[language]}
                language={language}
                onUpdate={(updateKey, updateValue) =>
                  props.onUpdate({
                    ...props.identifiable,
                    [updateKey]: {
                      ...props.identifiable[updateKey],
                      [language]: updateValue,
                    },
                  })
                }
              />
            ))}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
}

export default FileResourceForm
