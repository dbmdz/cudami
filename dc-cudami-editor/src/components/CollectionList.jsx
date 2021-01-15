import React, {useContext} from 'react'
import {Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaHashtag, FaImage} from 'react-icons/fa'

import AppContext from './AppContext'
import ListButtons from './ListButtons'
import PreviewImage from './PreviewImage'
import PublicationStatus from './PublicationStatus'
import {formatDate} from './utils'
import {typeToEndpointMapping} from '../api'

const CollectionList = ({
  enableMove,
  enableRemove,
  identifiables,
  language,
  onMove,
  onRemove,
  pageNumber,
  pageSize,
  parentType,
  showEdit,
  type,
}) => {
  const {t} = useTranslation()
  const {apiContextPath, uiLocale} = useContext(AppContext)
  const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
  return (
    <Table bordered className="mb-0" hover responsive size="sm" striped>
      <thead>
        <tr>
          <th className="text-right">
            <FaHashtag />
          </th>
          <th className="text-center">{t('status')}</th>
          <th className="text-center">
            <FaImage />
          </th>
          <th className="text-center">{t('label')}</th>
          <th className="text-center">{t('publicationStatus.startDate')}</th>
          <th className="text-center">{t('publicationStatus.endDate')}</th>
          <th className="text-center">{t('lastModified')}</th>
          <th className="text-center">{t('actions')}</th>
        </tr>
      </thead>
      <tbody>
        {identifiables.map(
          (
            {
              label,
              lastModified,
              previewImage,
              previewImageRenderingHints,
              publicationEnd,
              publicationStart,
              uuid,
            },
            index
          ) => {
            const publicationEndDate =
              publicationEnd && new Date(publicationEnd)
            const publicationStartDate =
              publicationStart && new Date(publicationStart)
            return (
              <tr key={uuid}>
                <td className="text-right">
                  {index + 1 + pageNumber * pageSize}
                </td>
                <td className="text-center">
                  <PublicationStatus
                    publicationEnd={publicationEndDate}
                    publicationStart={publicationStartDate}
                  />
                </td>
                <td className="text-center">
                  <PreviewImage
                    image={previewImage}
                    renderingHints={previewImageRenderingHints}
                    width={30}
                  />
                </td>
                <td>
                  {label[language] && (
                    <a href={`${viewBaseUrl}/${uuid}`}>{label[language]}</a>
                  )}
                </td>
                <td>{formatDate(publicationStartDate, uiLocale, true)}</td>
                <td>{formatDate(publicationEndDate, uiLocale, true)}</td>
                <td className="text-center">
                  {formatDate(new Date(lastModified), uiLocale)}
                </td>
                <td className="text-center">
                  <ListButtons
                    enableMove={enableMove}
                    enableRemove={enableRemove}
                    onMove={() => onMove(index)}
                    onRemove={() => onRemove(index)}
                    parentType={parentType}
                    showEdit={showEdit}
                    viewUrl={`${viewBaseUrl}/${uuid}`}
                  />
                </td>
              </tr>
            )
          }
        )}
      </tbody>
    </Table>
  )
}

export default CollectionList
