import {FormGroup, Input, Label} from 'reactstrap'

export default function Checkbox({checked, id, label, onChange}) {
  return (
    <FormGroup check>
      <Input
        id={id}
        type="checkbox"
        check
        checked={checked}
        onChange={(evt) => onChange(evt.target.checked)}
      />
      <Label check for={id}>
        {label}
      </Label>
    </FormGroup>
  )
}
