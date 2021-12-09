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
import {useContext} from 'use-context-selector'

import {generateSlug} from '../../api'
import {Context} from '../../state/Store'
import {EditableUrlAlias, UrlAlias} from '../UrlAliases'

const validateAliases = async (aliases, apiContextPath) => {
  const foo = await Promise.all(
    Object.entries(aliases).map(async ([language, [alias]]) => {
      const slug = await generateSlug(
        apiContextPath,
        language,
        alias.slug,
        alias.website?.uuid,
      )
      return [language, [{...alias, slug}]]
    }),
  )
  return Object.fromEntries(foo)
}

const ConfirmGeneratatedUrlAliasesDialog = ({
  isOpen,
  generatedUrlAliases = {},
  onChange,
  onConfirm,
  toggle,
}) => {
  const {apiContextPath} = useContext(Context)
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
        {editable ? (
          <Button
            color="primary"
            onClick={async () => {
              const validatedAliases = await validateAliases(
                generatedUrlAliases,
                apiContextPath,
              )
              onChange(validatedAliases)
              setEditable(false)
            }}
            size="xs"
          >
            {t('ready')}
          </Button>
        ) : (
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
          <Button className="mr-1" color="light" onClick={toggle}>
            {t('cancel')}
          </Button>
          <Button
            color="success"
            disabled={editable}
            onClick={() => {
              onConfirm()
              toggle()
            }}
          >
            {t('apply')}
          </Button>
        </ButtonGroup>
      </ModalFooter>
    </Modal>
  )
}

export default ConfirmGeneratatedUrlAliasesDialog
