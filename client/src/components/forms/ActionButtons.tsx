import classNames from 'classnames'
import {createPortal} from 'react-dom'
import {useTranslation} from 'react-i18next'
import {useInView} from 'react-intersection-observer'
import {Button, NavItem} from 'reactstrap'

interface ActionButtonsInNavbarProps {
  buttons: JSX.Element
  navbar: HTMLElement
}

const ActionButtonsInNavbar = ({
  buttons,
  navbar,
}: ActionButtonsInNavbarProps) => {
  return createPortal(
    <NavItem className="border-left ml-2 pl-3">{buttons}</NavItem>,
    navbar.querySelector('.navbar-nav') as HTMLElement,
  )
}

interface ActionButtonsProps {
  disabled?: boolean
  formId: string
}

const ActionButtons = ({disabled = false, formId}: ActionButtonsProps) => {
  const navbar = document.querySelector('.navbar') as HTMLElement
  const {inView, ref} = useInView({
    delay: 100,
    initialInView: true,
    rootMargin: `-${navbar?.offsetHeight}px 0px 0px 0px`,
    threshold: 1,
    trackVisibility: true,
  })

  const {t} = useTranslation()
  const buttonClasses = classNames(
    !inView && ['border', 'border-white', 'btn-sm'],
  )
  const buttons = (
    <Button
      className={buttonClasses}
      color="primary"
      disabled={disabled}
      form={formId}
      type="submit"
    >
      {t('save')}
    </Button>
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
