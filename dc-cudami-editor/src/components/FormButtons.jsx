import React from 'react'
import {Button, ButtonGroup, NavItem} from 'reactstrap'
import {createPortal} from 'react-dom'
import {useTranslation} from 'react-i18next'
import {useInView} from 'react-intersection-observer'

const FormButtonsInNavbar = ({buttons}) => {
  return createPortal(
    <NavItem className="border-left ml-2 pl-3">{buttons}</NavItem>,
    document.querySelector('.navbar-nav')
  )
}

const FormButtons = ({formId}) => {
  const {inView, ref} = useInView({
    delay: 100,
    initialInView: true,
    threshold: 1,
    trackVisibility: true,
  })
  const {t} = useTranslation()
  const buttons = (
    <ButtonGroup>
      <Button color="primary" form={formId} type="submit">
        {t('save')}
      </Button>
    </ButtonGroup>
  )
  return (
    <>
      {!inView && <FormButtonsInNavbar buttons={buttons} />}
      <div className="float-right" ref={ref}>
        {buttons}
      </div>
    </>
  )
}

export default FormButtons
