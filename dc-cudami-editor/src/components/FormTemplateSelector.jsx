import React from 'react'
import {
  Button,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  Label,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaEdit, FaPlus, FaTrash} from 'react-icons/fa'

const FormTemplateSelector = ({onClick, onRemove, templateName}) => {
  const {t} = useTranslation()
  const id = 'rendering-template-selector'
  return (
    <FormGroup>
      <Label className="d-block font-weight-bold" for={id}>
        {t('renderingTemplate')}
      </Label>
      {templateName ? (
        <InputGroup id={id} size="sm">
          <Input readOnly value={templateName} />
          <InputGroupAddon addonType="append">
            <Button className="px-1" onClick={onClick} outline size="sm">
              <FaEdit />
            </Button>
            <Button className="px-1" onClick={onRemove} outline size="sm">
              <FaTrash />
            </Button>
          </InputGroupAddon>
        </InputGroup>
      ) : (
        <Button id={id} onClick={onClick} size="sm">
          <FaPlus />
        </Button>
      )}
    </FormGroup>
  )
}

export default FormTemplateSelector
