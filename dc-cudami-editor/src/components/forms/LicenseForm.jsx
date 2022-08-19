import omit from 'lodash/omit'
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
import {
  addLanguage,
  removeLanguage,
  setActiveLanguage,
  setFeedbackMessage,
  toggleDialog,
} from '../../state/actions'
import {
  getActiveLanguage,
  getAvailableLanguages,
  getDialogsOpen,
  getExistingLanguages,
  getFeedbackMessage,
} from '../../state/selectors'
import {Context, DialogNames} from '../../state/Store'
import AddLanguageDialog from '../dialogs/AddLanguageDialog'
import RemoveLanguageDialog from '../dialogs/RemoveLanguageDialog'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import {cleanLocalizedText} from '../utils'
import ActionButtons from './ActionButtons'

const submitData = async (context, data, type, uuid) => {
  const response = await (uuid
    ? update(context, data, type)
    : save(context, data, type))
  return response
}

const LicenseForm = ({uuid}) => {
  const type = 'license'
  const activeLanguage = getActiveLanguage()
  const availableLanguages = getAvailableLanguages()
  const dialogsOpen = getDialogsOpen()
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
          const {error = false, uuid: uuidFromApi} = await submitData(
            apiContextPath,
            {...license, label: cleanLocalizedText(license.label)},
            type,
            uuid,
          )
          if (error) {
            return dispatch(
              setFeedbackMessage({
                color: 'danger',
                key: 'submitOfFormFailed',
              }),
            )
          }
          window.location.href = `${apiContextPath}${typeToEndpointMapping[type]}/${uuidFromApi}`
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
                  enableRemove={true}
                  key={language}
                  language={language}
                  toggle={(language) => dispatch(setActiveLanguage(language))}
                />
              ))}
              {availableLanguages.length > 0 && (
                <LanguageAdder
                  onClick={() =>
                    dispatch(toggleDialog(DialogNames.ADD_LANGUAGE))
                  }
                />
              )}
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
      <AddLanguageDialog
        addLanguage={(language) => {
          dispatch(addLanguage(language.name))
        }}
        availableLanguages={availableLanguages}
        isOpen={dialogsOpen.addLanguage}
        toggle={() => dispatch(toggleDialog(DialogNames.ADD_LANGUAGE))}
      />
      <RemoveLanguageDialog
        isOpen={dialogsOpen.removeLanguage}
        onConfirm={(language) => {
          dispatch(removeLanguage(language))
          setLicense({
            ...license,
            label: omit(license.label, [language]),
          })
        }}
        toggle={() => dispatch(toggleDialog(DialogNames.REMOVE_LANGUAGE))}
      />
    </>
  )
}

export default LicenseForm
