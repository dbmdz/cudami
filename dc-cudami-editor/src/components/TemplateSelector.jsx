import {useTranslation} from 'react-i18next'
import {FaEdit, FaPlus, FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  Label,
} from 'reactstrap'

const TemplateSelector = ({onClick, onRemove, templateName}) => {
  const {t} = useTranslation()
  const id = 'rendering-template-selector'
  return (
    <FormGroup>
      <Label className="d-block font-weight-bold">
        {t('types:renderingTemplate')}
      </Label>
      {templateName ? (
        <InputGroup id={id} size="sm">
          <Input readOnly value={templateName} />
          <InputGroupAddon addonType="append">
            <Button
              className="px-1"
              color="primary"
              onClick={onClick}
              outline
              size="sm"
            >
              <FaEdit />
            </Button>
            <Button
              className="px-1"
              color="primary"
              onClick={onRemove}
              outline
              size="sm"
            >
              <FaTrashAlt />
            </Button>
          </InputGroupAddon>
        </InputGroup>
      ) : (
        <Button color="primary" id={id} onClick={onClick} size="sm">
          <FaPlus />
        </Button>
      )}
    </FormGroup>
  )
}

export default TemplateSelector
