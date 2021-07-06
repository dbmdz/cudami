import '../../polyfills'

import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Alert, Col, Form, FormGroup, Label, Row} from 'reactstrap'

import {
  loadIdentifiable,
  saveIdentifiable,
  typeToEndpointMapping,
  updateIdentifiable,
  updateUser,
} from '../../api'
import Checkbox from '../Checkbox'
import FeedbackMessage from '../FeedbackMessage'
import InputWithLabel from '../InputWithLabel'
import ActionButtons from './ActionButtons'


const type = 'user'


export default function UserForm({allRoles, apiContextPath = '/', uuid}) {
  const [user, setUser] = useState(null)
  const [passwords, setPasswords] = useState({pwd1: null, pwd2: null})
  const [feedback, setFeedback] = useState()
  useEffect(() => {
    loadIdentifiable(apiContextPath, type, uuid).then(userObj => {
      setUser(userObj)
    })
  }, [])

  const setUserRole = function(role, selected) {
    let {roles: currentRoles = []} = user
    if (selected && !currentRoles.includes(role)) {
      currentRoles.push(role)
    }
    else if (!selected && currentRoles.includes(role)) {
      currentRoles.splice(currentRoles.indexOf(role), 1)
    }
    setUser({...user, roles: currentRoles})
  }

  const submitData = async function(apiContext, user, passwords) {
    let response
    if (user.uuid) {
      response = await updateUser(apiContext, user, Object.values(passwords).some(pwd => pwd) ? passwords : null)
    } else {
      response = await saveIdentifiable(context, data, null, null, type, false)
    }
    if ([200, 201].includes(response.status)) {
      window.location.href = `${apiContext}users/${response.returnObject.uuid}`
    } else if (response.status == 400) {
      // we got an error in the `returnObject`
      const {code, arguments: args} = response.returnObject
      let message = {color: "danger", key: "error"}
      if (code) {
        let errorKey = code.replace(/^error\./, "")
        message.key = errorKey
        message.values = args && {count: args[0]}
      }
      setFeedback(message)
    }
  }

  const {t} = useTranslation()
  if (!user) {
    return null
  }
  const formId = 'user-form'
  return (
    <>
    {feedback && <FeedbackMessage className="mb-2" message={feedback} onClose={() => setFeedback(undefined)}/>}
    <Form
      id={formId}
      onSubmit={(evt) => {
        evt.preventDefault()
        submitData(apiContextPath, user, passwords)
      }}
    >
      <Row form>
        <Col xs="6" sm="9">
          <h1>
            {uuid
              ? t('editUser', { email: user.email })
              : t('createUser')}
          </h1>
        </Col>
        <Col xs="6" sm="3">
          <ActionButtons formId={formId} />
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
            label={`${t("username")} / ${t("email")}`}
            onChange={email => setUser({...user, email})}
            required
            value={user.email ?? ''}
          />
        </Col>
      </Row>
      <Row form>
        <Col>
          <InputWithLabel
            id="lastname"
            labelKey="lastname"
            onChange={lastname => setUser({...user, lastname})}
            required
            value={user.lastname ?? ''}
          />
        </Col>
        <Col>
          <InputWithLabel
            id="firstname"
            labelKey="firstname"
            onChange={(firstname) =>
              setUser({...user, firstname})
            }
            required
            value={user.firstname ?? ''}
          />
        </Col>
      </Row>
      <FormGroup>
        <Label className="font-weight-bold">{t("roles")}</Label>
        {allRoles.map(role =>
          <Checkbox id={`chkbx_${role}`} label={role} checked={user.roles.includes(role)} onChange={isChecked => setUserRole(role, isChecked)} />
        )}
      </FormGroup>
      <FormGroup>
        <Label className="font-weight-bold">{t("status")}</Label>
        <Checkbox id="chkbx_active" label={t("activated")} checked={user.enabled} onChange={isChecked => setUser({...user, enabled: isChecked})} />
      </FormGroup>
      <Alert color="info">{t("passwordChangeInfo")}</Alert>
      <Row form>
        <Col>
          <InputWithLabel id="pwd1" type="password" labelKey="newPassword" value={passwords.pwd1} onChange={v => setPasswords({...passwords, pwd1: v})} />
        </Col>
        <Col>
          <InputWithLabel id="pwd2" type="password" labelKey="confirmPassword" value={passwords.pwd2} onChange={v => setPasswords({...passwords, pwd2: v})} />
        </Col>
      </Row>
    </Form>
    </>
  )
}
