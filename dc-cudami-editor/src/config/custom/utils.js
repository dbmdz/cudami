import React from 'react'
import {Button} from 'reactstrap'

import icons from '../icons'

export function createEditButton(onClick) {
  return (
    <Button color="light" onClick={onClick} size="sm">
      {icons.edit}
    </Button>
  )
}
