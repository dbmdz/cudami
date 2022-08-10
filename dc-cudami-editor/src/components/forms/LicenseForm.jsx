import {useEffect, useState} from 'react'
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
import {useContext} from 'use-context-selector'

import {getByUuid, save, typeToEndpointMapping, update} from '../../api'
import {setActiveLanguage, setFeedbackMessage} from '../../state/actions'
import {
  getActiveLanguage,
  getExistingLanguages,
  getFeedbackMessage,
} from '../../state/selectors'
import {Context} from '../../state/Store'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import LanguageTab from '../LanguageTab'
import ActionButtons from './ActionButtons'

const submitData = async (context, data, type, uuid) => {
  const {error = false} = await (uuid
    ? update(context, data, type)
    : save(context, data, type))
  return error
}

const LicenseForm = ({uuid}) => {
  const type = 'license'
  const activeLanguage = getActiveLanguage()
  const existingLanguages = getExistingLanguages()
  const feedbackMessage = getFeedbackMessage()
  const {apiContextPath, dispatch} = useContext(Context)
  const [license, setLicense] = useState()
  const {t} = useTranslation()
  useEffect(() => {
    getByUuid(apiContextPath, type, uuid).then((license) => {
      setLicense(license)
    })
  }, [])
  if (!license) {
    return null
  }
  const {acronym, label, url} = license
  const formId = 'license-form'
  return (
    <>
      {feedbackMessage && (
        <FeedbackMessage
          className="mb-2"
          message={feedbackMessage}
          onClose={() => dispatch(setFeedbackMessage(undefined))}
        />
      )}
      <Form
        id={formId}
        onSubmit={async (evt) => {
          evt.preventDefault()
          const error = await submitData(apiContextPath, license, type, uuid)
          if (error) {
            return dispatch(
              setFeedbackMessage({
                color: 'danger',
                key: 'submitOfFormFailed',
              }),
            )
          }
          window.location.href = `${apiContextPath}${typeToEndpointMapping[type]}`
        }}
      >
        <Row>
          <Col xs="6" sm="9">
            <h1>{uuid ? t('editLicense', {url}) : t('createLicense')}</h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons disabled={!url} formId={formId} />
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            <hr />
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            {uuid && (
              <InputWithLabel id="uuid" label="ID" readOnly value={uuid} />
            )}
            <InputWithLabel
              id="url"
              label="URL"
              onChange={(url) => setLicense({...license, url})}
              required
              type="url"
              value={url ?? ''}
            />
            <InputWithLabel
              id="acronym"
              labelKey="acronym"
              onChange={(acronym) => setLicense({...license, acronym})}
              value={acronym ?? ''}
            />
            <Nav tabs>
              {existingLanguages.map((language) => (
                <LanguageTab
                  activeLanguage={activeLanguage}
                  enableRemove={existingLanguages.length > 1}
                  key={language}
                  language={language}
                  toggle={(language) => dispatch(setActiveLanguage(language))}
                />
              ))}
            </Nav>
            <TabContent activeTab={activeLanguage}>
              {existingLanguages.map((language) => (
                <TabPane key={language} tabId={language}>
                  <Card className="bg-light mb-0">
                    <CardBody>
                      <InputWithLabel
                        id={`label-${language}`}
                        labelKey="label"
                        onChange={(lbl) =>
                          setLicense({
                            ...license,
                            label: {...label, [language]: lbl},
                          })
                        }
                        value={label?.[activeLanguage] ?? ''}
                      />
                    </CardBody>
                  </Card>
                </TabPane>
              ))}
            </TabContent>
          </Col>
        </Row>
      </Form>
    </>
  )
}

export default LicenseForm
