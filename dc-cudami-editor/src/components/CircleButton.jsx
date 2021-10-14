import './CircleButton.css'

import {Button} from 'reactstrap'

const CircleButton = ({disabled, children, onClick}) => (
  <Button
    className="align-items-center btn-circle d-flex justify-content-center"
    color={disabled ? 'light' : 'primary'}
    disabled={disabled}
    onClick={onClick}
  >
    {children}
  </Button>
)

export default CircleButton
