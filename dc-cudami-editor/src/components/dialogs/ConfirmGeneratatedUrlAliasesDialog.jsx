import groupBy from 'lodash-es/groupBy'
import {useEffect, useState} from 'react'
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
import {useContext} from 'use-context-selector'

import {generateSlug} from '../../api'
import {Context} from '../../state/Store'
import FeedbackMessage from '../FeedbackMessage'
import UrlAliases, {EditableUrlAliases} from '../UrlAliases'

const validateAliases = async (localizedAliases, apiContextPath) => {
  const validatedAliases = await Promise.all(
    Object.entries(localizedAliases).map(async ([language, aliases]) => {
      const validated = []
      for (let alias of aliases) {
        const slug = await generateSlug(
          apiContextPath,
          language,
          alias.slug,
          alias.website?.uuid,
        )
        validated.push({
          ...alias,
          slug,
        })
      }
      return [language, validated]
    }),
  )
  return Object.fromEntries(validatedAliases)
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
  const [feedbackMessage, setFeedbackMessage] = useState()
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setEditable(false)
  }
  useEffect(() => {
    const showDuplicateInformation =
      !editable &&
      Object.values(generatedUrlAliases).some(([{slug}]) => /-\d+$/.test(slug))
    if (showDuplicateInformation) {
      setFeedbackMessage({
        color: 'warning',
        key: 'duplicateInformation.urlAliases',
        values: {count: Object.keys(generatedUrlAliases).length},
      })
    } else {
      setFeedbackMessage(undefined)
    }
  }, [editable, generatedUrlAliases])
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
        {feedbackMessage && (
          <FeedbackMessage className="mb-2" message={feedbackMessage} />
        )}
        {Object.entries(generatedUrlAliases).map(([language, aliases]) => (
          <FormGroup key={language}>
            <Label className="align-middle mb-0">
              {t(`languageNames:${language}`)}
            </Label>
            {editable ? (
              <EditableUrlAliases
                aliasesToRender={groupBy(aliases, 'website.uuid')}
                onChange={(newSlug, oldSlug, websiteUuid) => {
                  const idx = aliases.findIndex(
                    ({slug, website}) =>
                      slug === oldSlug &&
                      websiteUuid === (website?.uuid ?? 'undefined'),
                  )
                  onChange({
                    ...generatedUrlAliases,
                    [language]: aliases.map((alias, currentIdx) => {
                      if (currentIdx === idx) {
                        return {...alias, slug: newSlug}
                      }
                      return alias
                    }),
                  })
                }}
              />
            ) : (
              <UrlAliases aliases={aliases} readOnly={true} showAll={true} />
            )}
          </FormGroup>
        ))}
      </ModalBody>
      <ModalFooter>
        <ButtonGroup>
          <Button className="mr-1" color="light" onClick={destroy}>
            {t('cancel')}
          </Button>
          <Button
            color="success"
            disabled={editable}
            onClick={() => {
              onConfirm()
              destroy()
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
