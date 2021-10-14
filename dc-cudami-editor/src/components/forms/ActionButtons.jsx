import classNames from 'classnames'
import {createPortal} from 'react-dom'
import {useTranslation} from 'react-i18next'
import {useInView} from 'react-intersection-observer'
import {Button, ButtonGroup, NavItem} from 'reactstrap'

const ActionButtonsInNavbar = ({buttons, navbar}) => {
  return createPortal(
    <NavItem className="border-left ml-2 pl-3">{buttons}</NavItem>,
    navbar.querySelector('.navbar-nav'),
  )
}

const ActionButtons = ({disabled = false, formId}) => {
  const navbar = document.querySelector('.navbar')
  const {inView, ref} = useInView({
    delay: 100,
    initialInView: true,
    rootMargin: `-${navbar.offsetHeight}px 0px 0px 0px`,
    threshold: 1,
    trackVisibility: true,
  })

  const {t} = useTranslation()
  const buttonClasses = classNames(!inView && ['border', 'border-white'])
  const buttons = (
    <ButtonGroup>
      <Button
        className={buttonClasses}
        color="primary"
        disabled={disabled}
        form={formId}
        type="submit"
      >
        {t('save')}
      </Button>
    </ButtonGroup>
  )
  const classes = classNames('float-right', {
    invisible: !inView,
    visible: inView,
  })
  return (
    <>
      {!inView && <ActionButtonsInNavbar buttons={buttons} navbar={navbar} />}
      <div className={classes} ref={ref}>
        {buttons}
      </div>
    </>
  )
}

export default ActionButtons
