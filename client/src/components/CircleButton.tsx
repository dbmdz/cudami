import './CircleButton.css'

import {ReactNode} from 'react'
import {Button} from 'reactstrap'

interface Props {
  disabled: boolean
  children: ReactNode
  onClick(): void
}

const CircleButton = ({disabled, children, onClick}: Props) => (
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
