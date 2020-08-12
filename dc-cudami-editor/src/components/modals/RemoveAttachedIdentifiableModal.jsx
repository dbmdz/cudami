import startCase from 'lodash/startCase'
import React from 'react'
import {Button, Modal, ModalBody, ModalHeader, ModalFooter} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const RemoveAttachedIdentifiableModal = ({
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

export default RemoveAttachedIdentifiableModal
