import '../../polyfills'

import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Alert, Col, Form, FormGroup, Label, Row} from 'reactstrap'

import {getUser, saveOrUpdateUser, typeToEndpointMapping} from '../../api'
import CheckboxWithLabel from '../CheckboxWithLabel'
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

const UserForm = ({allRoles, apiContextPath = '/', uuid}) => {
  useEffect(() => {
    getUser(apiContextPath, {uuid}).then((user) => {
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
  const updateRoles = (role, selected) => {
    return allRoles.filter((r) => {
      return r === role ? selected : roles.includes(r)
    })
  }
  const {email, enabled, firstname, lastname, roles} = user
  const requiredFields = [
    email,
    firstname,
    lastname,
    // when editing (uuid is set) the password inputs can be empty
    ...(uuid ? [] : [passwords.pwd1, passwords.pwd2]),
  ]
  const formId = 'user-form'
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
            <h1>{uuid ? t('editUser', {email}) : t('createUser')}</h1>
          </Col>
          <Col xs="6" sm="3">
            <ActionButtons
              disabled={requiredFields.some((field) => !field)}
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
            {uuid && (
              <InputWithLabel id="uuid" label="ID" readOnly value={uuid} />
            )}
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
        <FormGroup>
          <Label className="font-weight-bold">{t('roles')}</Label>
          {allRoles.map((role) => (
            <CheckboxWithLabel
              checked={roles.includes(role)}
              key={role}
              label={role}
              onChange={(isChecked) =>
                setUser({
                  ...user,
                  roles: updateRoles(role, isChecked),
                })
              }
            />
          ))}
        </FormGroup>
        <FormGroup>
          <Label className="font-weight-bold">{t('status')}</Label>
          <CheckboxWithLabel
            checked={enabled}
            label={t('activated')}
            onChange={(isChecked) => setUser({...user, enabled: isChecked})}
          />
        </FormGroup>
        {uuid && <Alert color="info">{t('passwordChangeInfo')}</Alert>}
        <Row form>
          <Col>
            <InputWithLabel
              id="pwd1"
              labelKey="password"
              onChange={(v) => setPasswords({...passwords, pwd1: v})}
              required={!uuid}
              type="password"
              value={passwords.pwd1 ?? ''}
            />
          </Col>
          <Col>
            <InputWithLabel
              id="pwd2"
              labelKey="confirmPassword"
              onChange={(v) => setPasswords({...passwords, pwd2: v})}
              required={!uuid}
              type="password"
              value={passwords.pwd2 ?? ''}
            />
          </Col>
        </Row>
      </Form>
    </>
  )
}

export default UserForm
