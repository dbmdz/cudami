import React from 'react'
import {Button, ButtonGroup} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaExchangeAlt, FaEye, FaPencilAlt, FaUnlink} from 'react-icons/fa'

import PreviewImage from './PreviewImage'

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
  const {t} = useTranslation()
  const viewUrl = `${apiContextPath}${type.toLowerCase()}s/${uuid}`
  return (
    <tr>
      <td className="text-right">{index}</td>
      <td className="text-center">
        <PreviewImage
          image={previewImage}
          renderingHints={previewImageRenderingHints}
          width={30}
        />
      </td>
      <td>{label && <a href={viewUrl}>{label}</a>}</td>
      <td>
        <ul className="list-inline mb-0">
          {identifiers.map(({id, namespace}) => (
            <li className="list-inline-item" key={`${namespace}:${id}`}>{`${
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
