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

import {Language} from '../../types'

interface Props {
  isOpen: boolean
  onConfirm(language: Language): void
  toggle(): void
}

const RemoveLanguageDialog = ({isOpen, onConfirm, toggle}: Props) => {
  const [language, setLanguage] = useState<Language>({
    displayName: '',
    name: '',
  })
  const {t} = useTranslation()
  useEffect(() => {
    const token = subscribe(
      'editor.show-remove-language-dialog',
      (_msg: string, lang: string) => {
        setLanguage({
          displayName: t(`languageNames:${lang}`),
          name: lang,
        })
        toggle()
      },
    )
    return () => {
      unsubscribe(token)
    }
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
