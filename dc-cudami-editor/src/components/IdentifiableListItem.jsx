import React from 'react'
import {Button, ButtonGroup} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaExchangeAlt, FaEye, FaPencilAlt, FaUnlink} from 'react-icons/fa'

import {getImageUrl} from './utils'

const IdentifiableListItem = ({
  apiContextPath,
  enableMove,
  enableRemove,
  identifiers,
  identifierTypes,
  index,
  label,
  lastModified,
  onMove,
  onRemove,
  parentType,
  previewImage,
  previewImageRenderingHints = {},
  showEdit,
  type,
  uiLocale,
  uuid,
}) => {
  const {altText, title, caption} = previewImageRenderingHints
  const previewImageWidth = 30
  const {t} = useTranslation()
  const viewUrl = `${apiContextPath}${type.toLowerCase()}s/${uuid}`
  return (
    <tr>
      <td className="text-right">{index}</td>
      <td className="text-center">
        <figure className="mb-0">
          <img
            alt={altText?.[uiLocale] ?? ''}
            src={
              previewImage
                ? getImageUrl(previewImage, `${previewImageWidth},`)
                : `${apiContextPath}images/no-image.png`
            }
            style={{maxWidth: `${previewImageWidth}px`}}
            title={title?.[uiLocale]}
          />
          {caption?.[uiLocale] && <figcaption>{caption[uiLocale]}</figcaption>}
        </figure>
      </td>
      <td>
        <a href={viewUrl}>{label}</a>
      </td>
      <td>
        <ul className="list-inline mb-0">
          {identifiers.map(({id, namespace}, index) => (
            <li className="list-inline-item" key={index}>{`${
              identifierTypes.find(
                (identifierType) => identifierType.namespace === namespace
              )?.label ?? namespace
            }: ${id}`}</li>
          ))}
        </ul>
      </td>
      <td className="text-center">
        {new Date(lastModified).toLocaleString(uiLocale, {hour12: false})}
      </td>
      <td className="text-center">
        <ButtonGroup>
          <Button className="p-0" color="link" href={viewUrl} title={t('view')}>
            <FaEye />
          </Button>
          {showEdit && (
            <Button
              className="ml-1 p-0"
              color="link"
              href={`${viewUrl}/edit`}
              title={t('edit')}
            >
              <FaPencilAlt />
            </Button>
          )}
        </ButtonGroup>
        <ButtonGroup>
          {enableMove && (
            <Button
              className="ml-1 p-0"
              color="link"
              onClick={onMove}
              title={t(`moveTo.${parentType}`)}
            >
              <FaExchangeAlt />
            </Button>
          )}
          {enableRemove && (
            <Button
              className="ml-1 p-0"
              color="link"
              onClick={onRemove}
              title={t(`removeFrom.${parentType}`)}
            >
              <FaUnlink />
            </Button>
          )}
        </ButtonGroup>
      </td>
    </tr>
  )
}

export default IdentifiableListItem
