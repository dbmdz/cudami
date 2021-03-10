import React from 'react'
import {Button} from 'reactstrap'

import icons from '../../config/icons'

const EditButton = ({onClick}) => {
  return (
    <Button color="light" onClick={onClick} size="sm">
      {icons.edit}
    </Button>
  )
}

export default EditButton
