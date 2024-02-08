import startCase from 'lodash-es/startCase'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

const RemoveAttachedIdentifiableDialog = ({
  isOpen,
  onConfirm,
  onToggle,
  parentType,
  type,
}) => {
  const {t} = useTranslation()
  const confirmMessage = t(`confirmRemove.${type}From${startCase(parentType)}`)
  return (
    <Modal isOpen={isOpen} toggle={onToggle}>
      <ModalHeader toggle={onToggle}>{t('warning')}</ModalHeader>
      <ModalBody>{confirmMessage}</ModalBody>
      <ModalFooter>
        <ButtonGroup>
          <Button className="mr-1" color="light" onClick={onToggle}>
            {t('no')}
          </Button>
          <Button
            color="danger"
            onClick={() => {
              onConfirm()
              onToggle()
            }}
          >
            {t('yes')}
          </Button>
        </ButtonGroup>
      </ModalFooter>
    </Modal>
  )
}

export default RemoveAttachedIdentifiableDialog
