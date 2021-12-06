import {useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  FormGroup,
  Label,
  ListGroup,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

import {EditableUrlAlias, UrlAlias} from '../UrlAliases'

const ConfirmGeneratatedUrlAliasesDialog = ({
  isOpen,
  generatedUrlAliases = {},
  onChange,
  onConfirm,
  toggle,
}) => {
  const [editable, setEditable] = useState(false)
  const {t} = useTranslation()
  return (
    <Modal isOpen={isOpen} size="lg">
      <ModalHeader
        cssModule={{
          'modal-title': 'd-flex justify-content-between modal-title w-100',
        }}
      >
        {t('generatedUrlAliases')}
        {!editable && (
          <Button color="primary" onClick={() => setEditable(true)} size="xs">
            {t('edit')}
          </Button>
        )}
      </ModalHeader>
      <ModalBody>
        {Object.entries(generatedUrlAliases).map(([language, [alias]]) => (
          <FormGroup key={language}>
            <Label className="align-middle mb-0">
              {t(`languageNames:${language}`)}
            </Label>
            {editable ? (
              <EditableUrlAlias
                onChange={(slug) =>
                  onChange({
                    ...generatedUrlAliases,
                    [language]: [{...alias, slug}],
                  })
                }
                slug={alias.slug}
                url={alias.website?.url}
              />
            ) : (
              <ListGroup>
                <UrlAlias
                  primary={alias.primary}
                  slug={alias.slug}
                  url={alias.website?.url}
                />
              </ListGroup>
            )}
          </FormGroup>
        ))}
      </ModalBody>
      <ModalFooter>
        <ButtonGroup>
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
