import React from 'react'
import {Button, ButtonGroup} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FormButtons = () => {
  const {t} = useTranslation()
  return (
    <div className="float-right">
      <ButtonGroup>
        <Button color="primary" type="submit">
          {t('save')}
        </Button>
      </ButtonGroup>
    </div>
  )
}

export default FormButtons
