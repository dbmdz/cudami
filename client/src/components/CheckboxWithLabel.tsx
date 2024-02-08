import {FormGroup, Input, Label} from 'reactstrap'

interface Props {
  checked: boolean
  label: string
  onChange(checked: boolean): void
}

const CheckboxWithLabel = ({checked, label, onChange}: Props) => (
  <FormGroup check>
    <Label check>
      <Input
        checked={checked}
        className="mr-1"
        onChange={(evt) => onChange(evt.target.checked)}
        type="checkbox"
      />
      {label}
    </Label>
  </FormGroup>
)

export default CheckboxWithLabel
