import {useTranslation} from 'react-i18next'
import {Col, Form, Nav, Row, TabContent, TabPane} from 'reactstrap'

import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import Teaser from '../Teaser'
import ActionButtons from './ActionButtons'

const WorkForm = ({
  activeLanguage,
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
  const canAddLanguage = false
  const {t} = useTranslation()
  return (
    <Form
      id={formId}
      onSubmit={(evt) => {
        evt.preventDefault()
        onSubmit()
      }}
    >
      <Row>
        <Col xs="6" sm="9">
          <h1>{identifiable.uuid ? t('editWork') : t('createWork')}</h1>
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
      <Row>
        <Col sm="12">
          {identifiable.uuid && (
            <InputWithLabel
              id="uuid"
              label="ID"
              readOnly
              value={identifiable.uuid}
            />
          )}
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
                  description={identifiable.description[language]}
                  enableUrlAliases={enableUrlAliases}
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
                  restrictedMenuForDescription={false}
                  updatePreviewImage={onUpdate}
                  urlAliases={identifiable.localizedUrlAliases?.[language]}
                />
              </TabPane>
            ))}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
}

export default WorkForm
