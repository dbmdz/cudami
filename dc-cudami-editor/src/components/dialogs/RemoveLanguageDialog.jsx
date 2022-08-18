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

const RemoveLanguageDialog = ({isOpen, onConfirm, toggle}) => {
  const [language, setLanguage] = useState()
  const {t} = useTranslation()
  useEffect(() => {
    const token = subscribe(
      'editor.show-remove-language-dialog',
      (_msg, lang) => {
        setLanguage({
          displayName: t(`languageNames:${lang}`),
          name: lang,
        })
        toggle()
      },
    )
    return () => unsubscribe(token)
  }, [])
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('warning')}</ModalHeader>
      <ModalBody>
        {t('confirmLanguageRemoval', {
          language: language?.displayName,
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
              onConfirm(language)
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

export default RemoveLanguageDialog
