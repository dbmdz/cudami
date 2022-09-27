import {useTranslation} from 'react-i18next'
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

import DateInput from '../DateInput'
import EditorWithLabel from '../editor/EditorWithLabel'
import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import Teaser from '../Teaser'
import Header from './Header'

const CollectionForm = ({
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
        buttonsDisabled={invalidLanguages.length > 0}
        formId={formId}
        heading={
          identifiable.uuid ? t('editCollection') : t('createCollection')
        }
      />
      {identifiable.uuid && (
        <Row>
          <Col sm="12">
            <InputWithLabel
              id="uuid"
              label="ID"
              readOnly
              value={identifiable.uuid}
            />
          </Col>
        </Row>
      )}
      <Row>
        <Col md="3" sm="6">
          <DateInput
            id="publication-start-date"
            label={t('publicationStatus:startDate')}
            onChange={(date) => onUpdate({publicationStart: date})}
            value={identifiable.publicationStart}
          />
        </Col>
        <Col md="3" sm="6">
          <DateInput
            id="publication-end-date"
            label={t('publicationStatus:endDate')}
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
                <Card className="border-top-0">
                  <CardBody>
                    <EditorWithLabel
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

export default CollectionForm
