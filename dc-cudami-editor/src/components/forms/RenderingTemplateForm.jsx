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
  loadDefaultLanguage,
  loadIdentifiable,
  saveIdentifiable,
  typeToEndpointMapping,
  updateIdentifiable,
} from '../../api'
import InputWithLabel from '../InputWithLabel'
import LanguageTab from '../LanguageTab'
import ActionButtons from './ActionButtons'

const loadData = async (context, type, uuid) => {
  const defaultLanguage = await loadDefaultLanguage(context)
  const template = await loadIdentifiable(context, type, uuid)
  return {
    defaultLanguage,
    template,
  }
}

const submitData = async (context, data, type, uuid) => {
  if (uuid) {
    await updateIdentifiable(context, data, type, false)
  } else {
    await saveIdentifiable(context, data, null, null, type, false)
  }
  window.location.href = `${context}${typeToEndpointMapping[type]}`
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
  const [template, setTemplate] = useState(null)
  const {t} = useTranslation()
  if (!template) {
    return null
  }
  const {description, label, name} = template
  const formId = 'rendering-template-form'
  return (
    <Form
      id={formId}
      onSubmit={(evt) => {
        evt.preventDefault()
        submitData(apiContextPath, template, type, uuid)
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
          <ActionButtons formId={formId} />
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
  )
}

export default RenderingTemplateForm
