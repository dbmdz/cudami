import mapValues from 'lodash-es/mapValues'
import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  FormText,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import InputWithFloatingLabel from '../InputWithFloatingLabel'

const AddIframeDialog = ({isOpen, toggle}) => {
  const initialAttributes = {
    height: '',
    src: '',
    title: '',
    width: '',
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
      'editor.show-iframe-dialog',
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
        {editing
          ? t('editor:insert.iframe.edit')
          : t('editor:insert.iframe.new')}
      </ModalHeader>
      <ModalBody>
        <Form
          onSubmit={(evt) => {
            evt.preventDefault()
            publish(
              'editor.add-iframe',
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
              name="iframe-url"
              onChange={(value) =>
                setAttributes({...attributes, src: value.trim()})
              }
              required
              type="url"
              value={attributes.src}
            />
          </FormGroup>
          <FormGroup>
            <InputWithFloatingLabel
              label={t('width')}
              name="iframe-width"
              onChange={(value) => setAttributes({...attributes, width: value})}
              value={attributes.width}
            />
            <FormText className="ml-1">
              {t('forExample')} <code>300px</code> or <code>50%</code>
            </FormText>
          </FormGroup>
          <FormGroup>
            <InputWithFloatingLabel
              label={t('height')}
              name="iframe-height"
              onChange={(value) =>
                setAttributes({...attributes, height: value})
              }
              value={attributes.height}
            />
            <FormText className="ml-1">
              {t('forExample')} <code>300px</code> or <code>50%</code>
            </FormText>
          </FormGroup>
          <FormGroup>
            <InputWithFloatingLabel
              label={t('tooltip')}
              name="iframe-title"
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

export default AddIframeDialog
