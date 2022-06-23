import '../../polyfills'

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

import {
  getByUuid,
  getDefaultLanguage,
  save,
  typeToEndpointMapping,
  update,
} from '../../api'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import LanguageTab from '../LanguageTab'
import ActionButtons from './ActionButtons'

const loadData = async (context, type, uuid) => {
  const defaultLanguage = await getDefaultLanguage(context)
  const template = await getByUuid(context, type, uuid)
  return {
    defaultLanguage,
    template,
  }
}

const submitData = async (context, data, type, uuid) => {
  const {error = false} = await (uuid
    ? update(context, data, type)
    : save(context, data, type))
  return error
}

const RenderingTemplateForm = ({apiContextPath = '/', uuid}) => {
  const type = 'renderingTemplate'
  useEffect(() => {
    loadData(apiContextPath, type, uuid).then(({defaultLanguage, template}) => {
      setDefaultLanguage(defaultLanguage)
      setTemplate(template)
    })
  }, [])
  const [defaultLanguage, setDefaultLanguage] = useState('')
  const [feedbackMessage, setFeedbackMessage] = useState()
  const [template, setTemplate] = useState(null)
  const {t} = useTranslation()
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
          onClose={() => setFeedbackMessage(undefined)}
        />
      )}
      <Form
        id={formId}
        onSubmit={async (evt) => {
          evt.preventDefault()
          const error = await submitData(apiContextPath, template, type, uuid)
          if (error) {
            return setFeedbackMessage({
              color: 'danger',
              key: 'submitOfFormFailed',
            })
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
              <LanguageTab
                activeLanguage={defaultLanguage}
                language={defaultLanguage}
                toggle={() => {}}
              />
            </Nav>
            <TabContent activeTab={defaultLanguage}>
              <TabPane tabId={defaultLanguage}>
                <Card className="bg-light">
                  <CardBody>
                    <InputWithLabel
                      id="label"
                      labelKey="label"
                      onChange={(label) =>
                        setTemplate({
                          ...template,
                          label: label
                            ? {
                                [defaultLanguage]: label,
                              }
                            : undefined,
                        })
                      }
                      value={label?.[defaultLanguage] ?? ''}
                    />
                    <InputWithLabel
                      id="description"
                      labelKey="description"
                      onChange={(description) =>
                        setTemplate({
                          ...template,
                          description: description
                            ? {
                                [defaultLanguage]: description,
                              }
                            : undefined,
                        })
                      }
                      value={description?.[defaultLanguage] ?? ''}
                    />
                  </CardBody>
                </Card>
              </TabPane>
            </TabContent>
          </Col>
        </Row>
      </Form>
    </>
  )
}

export default RenderingTemplateForm
