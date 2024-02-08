import '../../polyfills'

import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Col, Form, Row} from 'reactstrap'

import {getByUuid, save, typeToEndpointMapping, update} from '../../api'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import ActionButtons from './ActionButtons'

const submitData = async (context, data, type) => {
  const {error = false} = await (data.uuid
    ? update(context, data, type)
    : save(context, data, type))
  return error
}

const IdentifierTypeForm = ({apiContextPath = '/', uuid}) => {
  const type = 'identifierType'
  useEffect(() => {
    getByUuid(apiContextPath, type, uuid).then((identifierType) => {
      setIdentifierType(identifierType)
    })
  }, [])
  const [feedbackMessage, setFeedbackMessage] = useState()
  const [identifierType, setIdentifierType] = useState(null)
  const {t} = useTranslation()
  if (!identifierType) {
    return null
  }
  const {label, namespace, pattern} = identifierType
  const formId = 'identifier-type-form'
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
          const error = await submitData(apiContextPath, identifierType, type)
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
                ? t('editIdentifierType', {label})
                : t('createIdentifierType')}
            </h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons
              disabled={[label, namespace, pattern].some((field) => !field)}
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
            {uuid && (
              <InputWithLabel id="uuid" label="ID" readOnly value={uuid} />
            )}
            <InputWithLabel
              id="label"
              labelKey="label"
              onChange={(label) =>
                setIdentifierType({...identifierType, label})
              }
              required
              value={label ?? ''}
            />
          </Col>
        </Row>
        <Row form>
          <Col>
            <InputWithLabel
              id="namespace"
              labelKey="namespace"
              onChange={(namespace) =>
                setIdentifierType({...identifierType, namespace})
              }
              required
              value={namespace ?? ''}
            />
          </Col>
          <Col>
            <InputWithLabel
              id="pattern"
              labelKey="pattern"
              onChange={(pattern) =>
                setIdentifierType({...identifierType, pattern})
              }
              required
              value={pattern ?? ''}
            />
          </Col>
        </Row>
      </Form>
    </>
  )
}

export default IdentifierTypeForm
