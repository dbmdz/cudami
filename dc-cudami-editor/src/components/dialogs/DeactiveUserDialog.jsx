import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

const DeactiveUserDialog = ({email, isOpen, onConfirm, toggle}) => {
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('warning')}</ModalHeader>
      <ModalBody>{t('confirmUserDeactivation', {email})}</ModalBody>
      <ModalFooter>
        <ButtonGroup>
          <Button className="mr-1" color="light" onClick={toggle}>
            {t('no')}
          </Button>
          <Button
            color="danger"
            onClick={() => {
              onConfirm()
              toggle()
            }}
          >
            {t('yes')}
          </Button>
        </ButtonGroup>
      </ModalFooter>
    </Modal>
  )
}

export default DeactiveUserDialog
