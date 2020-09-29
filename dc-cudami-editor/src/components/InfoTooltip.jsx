import React from 'react'
import {Button, PopoverBody, UncontrolledPopover} from 'reactstrap'
import {FaQuestionCircle} from 'react-icons/fa'

const InfoTooltip = ({className = 'border', color = 'light', name, text}) => (
  <>
    <Button
      className={className}
      color={color}
      id={`${name}-tooltip`}
      type="button"
    >
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
