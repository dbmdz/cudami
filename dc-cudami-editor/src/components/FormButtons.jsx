import React from 'react'
import {Button, ButtonGroup} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FormButtons = props => {
  const {t} = useTranslation()
  return (
    <div className="float-right">
      <ButtonGroup>
        <Button
          className="mr-1"
          color="secondary"
          onClick={() => props.onAbort()}
          type="button"
        >
          {t('abort')}
        </Button>
        <Button color="primary" type="submit">
          {t('save')}
        </Button>
      </ButtonGroup>
    </div>
  )
}

export default FormButtons
