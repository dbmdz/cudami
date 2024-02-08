import '../../polyfills'

import omit from 'lodash-es/omit'
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
import {Context} from '../../state/Store'
import {DialogNames, RenderingTemplate} from '../../types'
import AddLanguageDialog from '../dialogs/AddLanguageDialog'
import RemoveLanguageDialog from '../dialogs/RemoveLanguageDialog'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import LanguageAdder from '../LanguageAdder'
import LanguageTab from '../LanguageTab'
import {cleanLocalizedText} from '../utils'
import ActionButtons from './ActionButtons'

interface Props {
  uuid: string
}

const submitData = async (
  context: string,
  data: RenderingTemplate,
  type: string,
) => {
  const {error = false} = await (data.uuid
    ? update(context, data, type)
    : save(context, data, type))
  return error
}

const RenderingTemplateForm = ({uuid}: Props) => {
  const type = 'renderingTemplate'
  const activeLanguage = getActiveLanguage()
  const availableLanguages = getAvailableLanguages()
  const dialogsOpen = getDialogsOpen()
  const existingLanguages = getExistingLanguages()
  const feedbackMessage = getFeedbackMessage()
  const {apiContextPath, dispatch} = useContext(Context)
  const [template, setTemplate] = useState<RenderingTemplate>()
  const {t} = useTranslation()
  useEffect(() => {
    getByUuid(apiContextPath, type, uuid).then((template) => {
      setTemplate(template)
    })
  }, [])
  if (!template) {
    return null
  }
  const {description, label, name} = template
  const formId = 'rendering-template-form'
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
          const error = await submitData(
            apiContextPath,
            {
              ...template,
              description: cleanLocalizedText(description),
              label: cleanLocalizedText(label),
            },
            type,
          )
          if (error) {
            dispatch(
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
            <h1>
              {uuid
                ? t('editRenderingTemplate', {name})
                : t('createRenderingTemplate')}
            </h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons disabled={!name} formId={formId} />
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
              id="name"
              labelKey="name"
              onChange={(name) => setTemplate({...template, name})}
              required
              value={name ?? ''}
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
                          setTemplate({
                            ...template,
                            label: {...label, [language]: lbl},
                          })
                        }
                        value={label?.[activeLanguage] ?? ''}
                      />
                      <InputWithLabel
                        id={`description-${language}`}
                        labelKey="description"
                        onChange={(dsc) =>
                          setTemplate({
                            ...template,
                            description: {...description, [language]: dsc},
                          })
                        }
                        value={description?.[activeLanguage] ?? ''}
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
          dispatch(addLanguage(language?.name ?? ''))
        }}
        availableLanguages={availableLanguages}
        isOpen={dialogsOpen.addLanguage}
        toggle={() => dispatch(toggleDialog(DialogNames.ADD_LANGUAGE))}
      />
      <RemoveLanguageDialog
        isOpen={dialogsOpen.removeLanguage}
        onConfirm={(language) => {
          dispatch(removeLanguage(language))
          setTemplate({
            ...template,
            description: omit(description, [language?.name ?? '']),
            label: omit(label, [language?.name ?? '']),
          })
        }}
        toggle={() => dispatch(toggleDialog(DialogNames.REMOVE_LANGUAGE))}
      />
    </>
  )
}

export default RenderingTemplateForm
