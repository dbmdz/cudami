import './UrlAliases.css'

import classNames from 'classnames'
import sortBy from 'lodash/sortBy'
import {publish} from 'pubsub-js'
import {useTranslation} from 'react-i18next'
import {FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  FormGroup,
  Input,
  Label,
  ListGroup,
  ListGroupItem,
} from 'reactstrap'

import {getShowAllUrlAliases} from '../state/selectors'
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
  onChangePrimary,
  onRemove,
  primary = false,
  slug,
  url = '',
}) => {
  const {i18n, t} = useTranslation()
  const uiLocale = i18n.language
  const showRemoveButton = !(lastPublished || !onRemove || primary)
  return (
    <ListGroupItem className="align-items-center d-flex justify-content-between">
      <FormGroup check className={classNames({'pl-0': !onChangePrimary})}>
        <Label check>
          {!!onChangePrimary && (
            <Input checked={primary} onChange={onChangePrimary} type="radio" />
          )}
          {`${url.replace(/\/$/, '')}/${slug}`}
        </Label>
      </FormGroup>
      {lastPublished && !primary && (
        <small>
          {t('publicationStatus:publishedOn', {
            date: formatDate(new Date(lastPublished), uiLocale, true),
          })}
        </small>
      )}
      {primary && <small>{t('primaryAlias')}</small>}
      {showRemoveButton && (
        <Button className="line-height-100 p-0" color="link" onClick={onRemove}>
          <FaTrashAlt />
        </Button>
      )}
    </ListGroupItem>
  )
}

const UrlAliases = ({
  aliases = [],
  aliasesToRender = {},
  onUpdate,
  readOnly = false,
  showAll = getShowAllUrlAliases(),
}) => {
  return (
    <>
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
                    primary={primary}
                    slug={slug}
                    url={website?.url}
                    {...(!readOnly && {
                      onChangePrimary: () =>
                        onUpdate(setNewPrimary(aliases, slug, website)),
                      onRemove: () =>
                        publish('editor.show-remove-urlalias-dialog', {
                          slug,
                          website,
                        }),
                    })}
                  />
                ))}
            </ListGroup>
          )
        })}
    </>
  )
}

export {UrlAlias}
export default UrlAliases
