import {subscribe, unsubscribe} from 'pubsub-js'
import {useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap'

const RemoveLanguageDialog = ({isOpen, onConfirm, toggle}) => {
  const [language, setLanguage] = useState('')
  useEffect(() => {
    const token = subscribe(
      'editor.show-remove-language-dialog',
      (_msg, language) => {
        setLanguage(language)
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('warning')}</ModalHeader>
      <ModalBody>
        {t('confirmLanguageRemoval', {
          language: t(`languageNames:${language}`),
        })}
      </ModalBody>
      <ModalFooter>
        <Button color="light" onClick={toggle}>
          {t('no')}
        </Button>
        <Button
          color="danger"
          onClick={() => {
            onConfirm(language)
            toggle()
          }}
        >
          {t('yes')}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

export default RemoveLanguageDialog
