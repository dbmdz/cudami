import startCase from 'lodash/startCase'
import React from 'react'
import {useTranslation} from 'react-i18next'
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap'

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
        <Button
          color="danger"
          onClick={() => {
            onConfirm()
            onToggle()
          }}
        >
          {t('yes')}
        </Button>
        <Button color="light" onClick={onToggle}>
          {t('no')}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

export default RemoveAttachedIdentifiableDialog
