import {useTranslation} from 'react-i18next'
import {Col, Form, Nav, Row, TabContent, TabPane} from 'reactstrap'

import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import Teaser from '../Teaser'
import Header from './Header'

const WebsiteForm = ({
  activeLanguage,
  canAddLanguage,
  existingLanguages,
  formId,
  identifiable,
  invalidLanguages,
  onAddLanguage,
  onSubmit,
  onToggleLanguage,
  onUpdate,
}) => {
  const {t} = useTranslation()
  return (
    <Form
      id={formId}
      onSubmit={(evt) => {
        evt.preventDefault()
        onSubmit()
      }}
    >
      <Header
        buttonsDisabled={invalidLanguages.length > 0 || !identifiable.url}
        formId={formId}
        heading={
          identifiable.uuid
            ? t('editWebsite', {url: identifiable.url})
            : t('createWebsite')
        }
      />
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
          <InputWithLabel
            îd="url"
            label="URL"
            onChange={(url) => onUpdate({...identifiable, url})}
            required
            type="url"
            value={identifiable.url}
          />
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

export default WebsiteForm
