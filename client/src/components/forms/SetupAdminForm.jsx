import '../../polyfills'

import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Col, Form, Row} from 'reactstrap'

import {getUser, saveOrUpdateUser, typeToEndpointMapping} from '../../api'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import ActionButtons from './ActionButtons'

const submitData = async (context, user, passwords) => {
  const {error, json} = await saveOrUpdateUser(
    context,
    user,
    Object.values(passwords).every((pwd) => pwd) ? passwords : null,
  )
  if (!error) {
    return {uuid: json.uuid}
  }
  // we got an error in the response json
  return {
    message: {
      color: 'danger',
      key: 'submitOfFormFailed',
      text: json.defaultMessage,
    },
  }
}

const UserForm = ({apiContextPath = '/'}) => {
  useEffect(() => {
    getUser(apiContextPath, {admin: true}).then((user) => {
      setUser(user)
    })
  }, [])
  const [feedbackMessage, setFeedbackMessage] = useState()
  const [passwords, setPasswords] = useState({})
  const [user, setUser] = useState()
  const {t} = useTranslation()
  if (!user) {
    return null
  }
  const {email, firstname, lastname} = user
  const formId = 'setup-admin-form'
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
          const {message, uuid} = await submitData(
            apiContextPath,
            user,
            passwords,
          )
          if (message) {
            return setFeedbackMessage(message)
          }
          window.location.href = `${apiContextPath}${typeToEndpointMapping.user}/${uuid}`
        }}
      >
        <Row form>
          <Col xs="6" sm="9">
            <h1>{t('createUser')}</h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons
              disabled={[
                email,
                firstname,
                lastname,
                passwords.pwd1,
                passwords.pwd2,
              ].some((field) => !field)}
              formId={formId}
            />
          </Col>
        </Row>
        <Row form>
          <Col sm="12">
            <hr />
          </Col>
        </Row>
        <Row form>
          <Col sm="12">
            <InputWithLabel
              id="email"
              label={`${t('username')} / ${t('email')}`}
              onChange={(email) => setUser({...user, email})}
              required
              value={email ?? ''}
            />
          </Col>
        </Row>
        <Row form>
          <Col>
            <InputWithLabel
              id="lastname"
              labelKey="lastname"
              onChange={(lastname) => setUser({...user, lastname})}
              required
              value={lastname ?? ''}
            />
          </Col>
          <Col>
            <InputWithLabel
              id="firstname"
              labelKey="firstname"
              onChange={(firstname) => setUser({...user, firstname})}
              required
              value={firstname ?? ''}
            />
          </Col>
        </Row>
        <Row form>
          <Col>
            <InputWithLabel
              id="pwd1"
              labelKey="password"
              onChange={(v) => setPasswords({...passwords, pwd1: v})}
              required
              type="password"
              value={passwords?.pwd1 ?? ''}
            />
          </Col>
          <Col>
            <InputWithLabel
              id="pwd2"
              labelKey="confirmPassword"
              onChange={(v) => setPasswords({...passwords, pwd2: v})}
              required
              type="password"
              value={passwords?.pwd2 ?? ''}
            />
          </Col>
        </Row>
      </Form>
    </>
  )
}

export default UserForm
