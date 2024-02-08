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
        {t('types:renderingTemplate', {count: 1})}
      </Label>
      {templateName ? (
        <InputGroup id={id}>
          <Input readOnly value={templateName} />
          <InputGroupAddon addonType="append">
            <Button
              className="align-items-center d-flex p-1"
              color="primary"
              onClick={onClick}
              outline
              size="sm"
            >
              <FaEdit />
            </Button>
            <Button
              className="align-items-center d-flex p-1"
              color="danger"
              onClick={onRemove}
              outline
              size="sm"
            >
              <FaTrashAlt />
            </Button>
          </InputGroupAddon>
        </InputGroup>
      ) : (
        <Button
          className="align-items-center d-flex p-2"
          color="primary"
          id={id}
          onClick={onClick}
        >
          <FaPlus />
        </Button>
      )}
    </FormGroup>
  )
}

export default TemplateSelector
