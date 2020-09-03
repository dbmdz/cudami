import React from 'react'
import {Button, Popover, PopoverBody} from 'reactstrap'
import {FaQuestionCircle} from 'react-icons/fa'

const InfoTooltip = ({isOpen, name, text, toggle}) => {
  return (
    <>
      <Button
        className="border"
        color="light"
        id={`${name}-tooltip`}
        type="button"
      >
        <FaQuestionCircle />
      </Button>
      <Popover
        isOpen={isOpen}
        placement="left"
        target={`${name}-tooltip`}
        toggle={toggle}
      >
        <PopoverBody>{text}</PopoverBody>
      </Popover>
    </>
  )
}

export default InfoTooltip
