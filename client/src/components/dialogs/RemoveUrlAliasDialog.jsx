import {subscribe, unsubscribe} from 'pubsub-js'
import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

const RemoveUrlAliasDialog = ({activeLanguage, isOpen, onConfirm, toggle}) => {
  const [state, setState] = useState({})
  useEffect(() => {
    const token = subscribe(
      'editor.show-remove-urlalias-dialog',
      (_msg, data) => {
        setState(data)
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('warning')}</ModalHeader>
      <ModalBody className="overflow-auto">
        {t('confirmUrlAliasRemoval', {
          language: t(`languageNames:${activeLanguage}`),
          slug: state.slug,
          url: state.website?.url ?? '',
        })}
      </ModalBody>
      <ModalFooter>
        <ButtonGroup>
          <Button className="mr-1" color="light" onClick={toggle}>
            {t('no')}
          </Button>
          <Button
            color="danger"
            onClick={() => {
              onConfirm(state.slug, state.website?.uuid)
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

export default RemoveUrlAliasDialog
