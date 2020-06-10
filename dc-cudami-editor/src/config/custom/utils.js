import React from 'react'
import {Button} from 'reactstrap'

import icons from '../icons'

export function createEditButton(onClick) {
  return (
    <Button
      className="position-absolute"
      color="light"
      onClick={onClick}
      size="sm"
    >
      {icons.edit}
    </Button>
  )
}
