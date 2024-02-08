import {FaQuestionCircle} from 'react-icons/fa'
import {Button, PopoverBody, UncontrolledPopover} from 'reactstrap'

interface Props {
  className?: string
  color?: string
  name: string
  text: string
}

const InfoTooltip = ({
  className = 'align-items-center border d-flex',
  color = 'light',
  name,
  text,
}: Props) => (
  <>
    <Button className={className} color={color} id={`${name}-tooltip`}>
      <FaQuestionCircle />
    </Button>
    <UncontrolledPopover
      placement="left"
      target={`${name}-tooltip`}
      trigger="focus"
    >
      <PopoverBody>{text}</PopoverBody>
    </UncontrolledPopover>
  </>
)

export default InfoTooltip
