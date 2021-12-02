import groupBy from 'lodash/groupBy'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  FormGroup,
  Label,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

import UrlAliases from '../UrlAliases'

const ConfirmGeneratatedUrlAliasesDialog = ({
  isOpen,
  generatedUrlAliases = {},
  onConfirm,
  toggle,
}) => {
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} size="lg" toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('generatedUrlAliases')}</ModalHeader>
      <ModalBody>
        {Object.entries(generatedUrlAliases).map(([language, aliases]) => (
          <FormGroup key={language}>
            <Label className="align-middle mb-0">
              {t(`languageNames:${language}`)}
            </Label>
            <UrlAliases
              aliasesToRender={groupBy(aliases, 'website.uuid')}
              readOnly={true}
              showAll={true}
            />
          </FormGroup>
        ))}
      </ModalBody>
      <ModalFooter>
        <ButtonGroup>
          <Button className="mr-1" color="light" onClick={toggle}>
            {t('no')}
          </Button>
          <Button
            color="success"
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

export default ConfirmGeneratatedUrlAliasesDialog
