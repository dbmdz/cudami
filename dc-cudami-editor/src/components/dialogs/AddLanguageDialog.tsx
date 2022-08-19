import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

import {Language} from '../../types'

interface Props {
  addLanguage(language?: Language): void
  availableLanguages: Language[]
  isOpen: boolean
  toggle(): void
}

const AddLanguageDialog = ({
  addLanguage,
  availableLanguages,
  isOpen,
  toggle,
}: Props) => {
  const [selectedLanguage, setSelectedLanguage] = useState('')
  const {t} = useTranslation()
  return (
    <Modal
      isOpen={isOpen}
      onOpened={() => setSelectedLanguage(availableLanguages[0].name)}
      toggle={toggle}
    >
      <ModalHeader toggle={toggle}>{t('chooseLanguage')}</ModalHeader>
      <ModalBody>
        <Form
          onSubmit={(evt) => {
            evt.preventDefault()
            addLanguage(
              availableLanguages.find(
                (language) => language.name === selectedLanguage,
              ),
            )
            toggle()
          }}
        >
          <FormGroup>
            <Input
              onChange={(evt) => setSelectedLanguage(evt.target.value)}
              type="select"
            >
              {availableLanguages.map((language) => (
                <option key={language.name} value={language.name}>
                  {language.displayName}
                </option>
              ))}
            </Input>
          </FormGroup>
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={toggle}>
              {t('cancel')}
            </Button>
            <Button color="primary" type="submit">
              {t('add')}
            </Button>
          </ButtonGroup>
        </Form>
      </ModalBody>
    </Modal>
  )
}

export default AddLanguageDialog
