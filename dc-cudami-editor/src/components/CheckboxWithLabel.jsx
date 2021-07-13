import {FormGroup, Input, Label} from 'reactstrap'

const CheckboxWithLabel = ({checked, label, onChange}) => (
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
