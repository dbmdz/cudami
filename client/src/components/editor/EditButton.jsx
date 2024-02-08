import {useTranslation} from 'react-i18next'
import {Button} from 'reactstrap'

import icons from '../../config/icons'

const EditButton = ({onClick, titleKey}) => {
  const {t} = useTranslation()
  return (
    <Button
      color="light"
      onClick={onClick}
      size="sm"
      title={t(`editor:${titleKey}`)}
    >
      {icons.edit}
    </Button>
  )
}

export default EditButton
