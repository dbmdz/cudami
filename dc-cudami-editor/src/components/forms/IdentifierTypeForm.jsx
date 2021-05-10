import '../../polyfills'

import React, {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Col, Form, Row} from 'reactstrap'

import {
  loadIdentifiable,
  saveIdentifiable,
  typeToEndpointMapping,
  updateIdentifiable,
} from '../../api'
import InputWithLabel from '../InputWithLabel'
import ActionButtons from './ActionButtons'

const submitData = async (context, data, type, uuid) => {
  if (uuid) {
    await updateIdentifiable(context, data, type, false)
  } else {
    await saveIdentifiable(context, data, null, null, type, false)
  }
  window.location.href = `${context}${typeToEndpointMapping[type]}`
}

const IdentifierTypeForm = ({apiContextPath = '/', uuid}) => {
  const type = 'identifierType'
  useEffect(() => {
    loadIdentifiable(apiContextPath, type, uuid).then((identifierType) => {
      setIdentifierType(identifierType)
    })
  }, [])
  const [identifierType, setIdentifierType] = useState(null)
  const {t} = useTranslation()
  if (!identifierType) {
    return null
  }
  const {label, namespace, pattern} = identifierType
  const formId = 'identifier-type-form'
  return (
    <Form
      id={formId}
      onSubmit={(evt) => {
        evt.preventDefault()
        submitData(apiContextPath, identifierType, type, uuid)
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
            id="label"
            labelKey="label"
            onChange={(label) => setIdentifierType({...identifierType, label})}
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
  )
}

export default IdentifierTypeForm
