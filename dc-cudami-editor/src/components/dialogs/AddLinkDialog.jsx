import mapValues from 'lodash-es/mapValues'
import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import InputWithFloatingLabel from '../InputWithFloatingLabel'

const AddLinkDialog = ({isOpen, toggle}) => {
  const initialAttributes = {
    href: '',
    title: '',
  }
  const [attributes, setAttributes] = useState(initialAttributes)
  const [editing, setEditing] = useState(false)
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setAttributes(initialAttributes)
  }
  useEffect(() => {
    const token = subscribe(
      'editor.show-link-dialog',
      (_msg, {attributes: attrs = {}, editing = false} = {}) => {
        setAttributes({
          ...attributes,
          ...mapValues(attrs, (value) => value ?? ''),
        })
        setEditing(editing)
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  return (
    <Modal isOpen={isOpen} toggle={destroy}>
      <ModalHeader toggle={destroy}>
        {t(`editor:marks.link.${editing ? 'edit' : 'insert'}`)}
      </ModalHeader>
      <ModalBody>
        <Form
          onSubmit={(evt) => {
            evt.preventDefault()
            publish(
              'editor.add-link',
              mapValues(attributes, (value) =>
                value !== '' ? value : undefined,
              ),
            )
            destroy()
          }}
        >
          <FormGroup>
            <InputWithFloatingLabel
              label="URL"
              name="link-url"
              onChange={(value) =>
                setAttributes({...attributes, href: value.trim()})
              }
              pattern="^(https?://|mailto:|/|#).*$"
              required
              value={attributes.href}
            />
          </FormGroup>
          <FormGroup>
            <InputWithFloatingLabel
              label={t('tooltip')}
              name="link-title"
              onChange={(value) => setAttributes({...attributes, title: value})}
              value={attributes.title}
            />
          </FormGroup>
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={destroy}>
              {t('cancel')}
            </Button>
            <Button color="primary" type="submit">
              {editing ? t('save') : t('add')}
            </Button>
          </ButtonGroup>
        </Form>
      </ModalBody>
    </Modal>
  )
}

export default AddLinkDialog
