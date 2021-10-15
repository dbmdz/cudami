import {useTranslation} from 'react-i18next'
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap'

const DeactiveUserDialog = ({email, isOpen, onConfirm, toggle}) => {
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('warning')}</ModalHeader>
      <ModalBody>{t('confirmUserDeactivation', {email})}</ModalBody>
      <ModalFooter>
        <Button color="light" onClick={toggle}>
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
      </ModalFooter>
    </Modal>
  )
}

export default DeactiveUserDialog
