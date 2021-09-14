import './UrlAliases.css'

import groupBy from 'lodash/groupBy'
import sortBy from 'lodash/sortBy'
import {publish} from 'pubsub-js'
import {useContext, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  Col,
  FormGroup,
  Input,
  Label,
  ListGroup,
  ListGroupItem,
  Row,
} from 'reactstrap'

import AppContext from './AppContext'
import {formatDate} from './utils'

const setNewPrimary = (aliases, slug, website = {}) =>
  aliases.map((alias) => {
    let primary = alias.primary
    if (alias.website?.uuid === website?.uuid) {
      primary = alias.slug === slug
    }
    return {...alias, primary}
  })

const sortByWebsite = (
  [uuid1, [{website: website1}]],
  [uuid2, [{website: website2}]],
) => {
  if (uuid1 === 'undefined') {
    return 1
  }
  if (uuid2 === 'undefined') {
    return -1
  }
  return website1.url > website2.url ? 1 : -1
}

const UrlAlias = ({
  lastPublished,
  onChange,
  onRemove,
  primary = false,
  slug,
  url = '',
}) => {
  const {uiLocale} = useContext(AppContext)
  const {t} = useTranslation()
  const showRemoveButton = !(lastPublished || primary)
  return (
    <ListGroupItem className="d-flex justify-content-between">
      <FormGroup check>
        <Label check>
          <Input
            checked={primary}
            name={slug}
            onChange={onChange}
            type="radio"
          />
          {`${url}/${slug}`}
        </Label>
      </FormGroup>
      {lastPublished && !primary && (
        <small>
          {t('publicationStatus:publishedOn', {
            date: formatDate(new Date(lastPublished), uiLocale, true),
          })}
        </small>
      )}
      {primary && <small>{t('isPrimaryAlias')}</small>}
      {showRemoveButton && (
        <Button className="line-height-100 p-0" color="link" onClick={onRemove}>
          <FaTrashAlt />
        </Button>
      )}
    </ListGroupItem>
  )
}

const UrlAliases = ({aliases = [], onUpdate}) => {
  const [showAll, setShowAll] = useState(false)
  const {t} = useTranslation()
  const aliasesToRender = groupBy(aliases, 'website.uuid')
  const showExpandButton = Object.values(aliasesToRender).some(
    (listOfAliases) => listOfAliases.length > 1,
  )
  return (
    <Row className="mt-3">
      <Col sm={12}>
        <FormGroup className="mb-1">
          <Label className="align-middle mb-0">{t('urlAliases')}</Label>
          {showExpandButton && (
            <Button
              className="ml-2"
              color="primary"
              onClick={() => setShowAll(!showAll)}
              size="xs"
            >
              {showAll ? t('showPrimaryAliases') : t('showAll')}
            </Button>
          )}
          {Object.entries(aliasesToRender)
            .sort(sortByWebsite)
            .map(([, listOfAliases]) => {
              const website = listOfAliases[0].website
              return (
                <ListGroup className="my-2" key={website?.uuid ?? 'default'}>
                  {sortBy(listOfAliases, ['slug'])
                    .filter(({primary}) => showAll || primary)
                    .map(({lastPublished, primary, slug}) => (
                      <UrlAlias
                        key={slug}
                        lastPublished={lastPublished}
                        onChange={() =>
                          onUpdate(setNewPrimary(aliases, slug, website))
                        }
                        onRemove={() =>
                          publish('editor.show-remove-urlalias-dialog', {
                            slug,
                            website,
                          })
                        }
                        primary={primary}
                        slug={slug}
                        url={website?.url}
                      />
                    ))}
                </ListGroup>
              )
            })}
        </FormGroup>
      </Col>
    </Row>
  )
}

export default UrlAliases
