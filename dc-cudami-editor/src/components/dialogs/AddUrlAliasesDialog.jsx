import './AddUrlAliasesDialog.css'

import classNames from 'classnames'
import omit from 'lodash/omit'
import pick from 'lodash/pick'
import {subscribe, unsubscribe} from 'pubsub-js'
import {useContext, useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaCheck, FaGlobe, FaLink, FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  FormFeedback,
  Input,
  InputGroup,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  ListGroupItemText,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

import {generateSlug, loadRootIdentifiables} from '../../api'
import AppContext from '../AppContext'
import Autocomplete from '../Autocomplete'
import CircleButton from '../CircleButton'
import FeedbackMessage from '../FeedbackMessage'
import {UrlAlias} from '../UrlAliases'

const AddUrlAliasesDialog = ({
  activeLanguage,
  existingUrlAliases,
  isOpen,
  onSubmit,
  parentWebsite,
  target,
  toggle,
}) => {
  const initialUrlAlias = {
    invalid: true,
    slug: '',
    targetLanguage: activeLanguage,
    website:
      parentWebsite &&
      pick(parentWebsite, ['entityType', 'type', 'url', 'uuid']),
    ...target,
  }
  useEffect(() => {
    const token = subscribe('editor.show-add-urlaliases-dialog', () => {
      setActiveStep(0)
      setNewUrlAlias(initialUrlAlias)
      toggle()
    })
    return () => unsubscribe(token)
  }, [])
  const {apiContextPath} = useContext(AppContext)
  const [activeStep, setActiveStep] = useState(0)
  const [newUrlAlias, setNewUrlAlias] = useState(initialUrlAlias)
  useEffect(() => {
    setNewUrlAlias({
      ...newUrlAlias,
      invalid:
        !newUrlAlias.slug ||
        existingUrlAliases.some(
          ({slug, website}) =>
            slug === newUrlAlias.slug &&
            newUrlAlias.website?.uuid === website?.uuid,
        ),
    })
  }, [newUrlAlias.slug, newUrlAlias.website])
  const {t} = useTranslation()
  const steps = [
    {Icon: FaLink, label: t('slug'), name: 'slug'},
    {Icon: FaCheck, name: 'confirm'},
  ]
  if (!parentWebsite) {
    steps.unshift({Icon: FaGlobe, label: t('types:website'), name: 'website'})
  }
  const stepName = steps[activeStep].name
  return (
    <Modal isOpen={isOpen} size="lg" toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('addUrlAlias')}</ModalHeader>
      <ModalBody>
        <ListGroup horizontal>
          {steps.map(({Icon, label, name}, idx) => {
            const enabled = idx <= activeStep
            return (
              <ListGroupItem
                className={classNames(
                  'align-items-center border-0 d-flex flex-column flex-fill pt-0 with-center-line',
                  {
                    enabled,
                  },
                )}
                key={name}
              >
                <CircleButton
                  disabled={!enabled}
                  onClick={() => setActiveStep(idx)}
                >
                  <Icon />
                </CircleButton>
                <ListGroupItemText className="mb-0">
                  {label ?? t(name)}
                </ListGroupItemText>
              </ListGroupItem>
            )
          })}
        </ListGroup>
        {stepName === 'website' && (
          <>
            <FeedbackMessage
              className="mb-2"
              message={{
                color: 'info',
                key: 'noWebsiteNeeded',
              }}
            />
            <Autocomplete
              activeLanguage={activeLanguage}
              maxElements={5}
              onSelect={(website) => {
                setNewUrlAlias({
                  ...newUrlAlias,
                  website: pick(website, ['entityType', 'type', 'url', 'uuid']),
                })
                setActiveStep(activeStep + 1)
              }}
              placeholder={t('websiteSearchTerm')}
              search={(contextPath, searchTerm, pageNumber, pageSize) =>
                loadRootIdentifiables(
                  contextPath,
                  'website',
                  pageNumber,
                  pageSize,
                  searchTerm,
                )
              }
            />
          </>
        )}
        {stepName === 'slug' && (
          <>
            <InputGroup className="mb-1">
              <Input
                readOnly
                required
                type="url"
                value={newUrlAlias.website?.url ?? ''}
              />
              {newUrlAlias.website && !parentWebsite && (
                <InputGroupAddon addonType="append">
                  <Button
                    className="align-items-center d-flex px-1"
                    color="primary"
                    onClick={() => {
                      setNewUrlAlias({
                        ...newUrlAlias,
                        website: undefined,
                      })
                      setActiveStep(activeStep - 1)
                    }}
                    outline
                  >
                    <FaTrashAlt />
                  </Button>
                </InputGroupAddon>
              )}
            </InputGroup>
            <Input
              className="rounded"
              invalid={newUrlAlias.invalid}
              onChange={(evt) =>
                setNewUrlAlias({
                  ...newUrlAlias,
                  primary: !existingUrlAliases.some(
                    ({primary, website: ws}) =>
                      newUrlAlias.website?.uuid === ws?.uuid && primary,
                  ),
                  slug: evt.target.value,
                })
              }
              placeholder={t('slug')}
              value={newUrlAlias.slug}
            />
            <FormFeedback>{t('noDuplicateSlugs')}</FormFeedback>
          </>
        )}
        {stepName === 'confirm' && (
          <ListGroup>
            <UrlAlias
              readOnly={true}
              primary={newUrlAlias.primary}
              slug={newUrlAlias.slug}
              url={newUrlAlias.website?.url}
            />
          </ListGroup>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="light" onClick={toggle}>
          {t('cancel')}
        </Button>
        {stepName === 'website' && (
          <Button color="primary" onClick={() => setActiveStep(activeStep + 1)}>
            {t('next')}
          </Button>
        )}
        {stepName === 'slug' && (
          <Button
            color="primary"
            disabled={newUrlAlias.invalid}
            onClick={async () => {
              const slug = await generateSlug(
                apiContextPath,
                activeLanguage,
                newUrlAlias.slug,
                newUrlAlias.website?.uuid,
              )
              setNewUrlAlias({...newUrlAlias, slug})
              setActiveStep(activeStep + 1)
            }}
          >
            {t('next')}
          </Button>
        )}
        {stepName === 'confirm' && (
          <Button
            color="primary"
            onClick={() => {
              onSubmit(omit(newUrlAlias, 'invalid'))
              toggle()
            }}
          >
            {t('add')}
          </Button>
        )}
      </ModalFooter>
    </Modal>
  )
}

export default AddUrlAliasesDialog
