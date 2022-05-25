import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Input,
  Label,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

const AddTableDialog = ({isOpen, toggle}) => {
  const initialAttributes = {
    columns: 2,
    rows: 2,
  }
  const [attributes, setAttributes] = useState(initialAttributes)
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setAttributes(initialAttributes)
  }
  useEffect(() => {
    const token = subscribe('editor.show-table-dialog', () => {
      toggle()
    })
    return () => unsubscribe(token)
  }, [])
  return (
    <Modal isOpen={isOpen} toggle={destroy}>
      <ModalHeader toggle={destroy}>{t('editor:insert.table')}</ModalHeader>
      <ModalBody>
        <Form
          onSubmit={(evt) => {
            evt.preventDefault()
            publish('editor.add-table', attributes)
            destroy()
          }}
        >
          <FormGroup>
            <Label className="font-weight-bold" for="table-rows">
              {t('numberOfRows')}
            </Label>
            <Input
              id="table-rows"
              min="1"
              onChange={(evt) =>
                setAttributes({...attributes, rows: parseInt(evt.target.value)})
              }
              required
              type="number"
              value={attributes.rows}
            />
          </FormGroup>
          <FormGroup>
            <Label className="font-weight-bold" for="table-columns">
              {t('numberOfColumns')}
            </Label>
            <Input
              id="table-columns"
              min="1"
              onChange={(evt) =>
                setAttributes({
                  ...attributes,
                  columns: parseInt(evt.target.value),
                })
              }
              required
              type="number"
              value={attributes.columns}
            />
          </FormGroup>
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={destroy}>
              {t('cancel')}
            </Button>
            <Button color="primary" type="submit">
              {t('add')}
            </Button>
          </ButtonGroup>
        </Form>
      </ModalBody>
    </Modal>
  )
}

export default AddTableDialog
