import {useContext} from 'react'
import {useTranslation} from 'react-i18next'
import {FaHashtag, FaImage} from 'react-icons/fa'
import {Table} from 'reactstrap'

import {typeToEndpointMapping} from '../../api'
import AppContext from '../AppContext'
import IdentifierList from '../IdentifierList'
import PreviewImage from '../PreviewImage'
import {formatDate} from '../utils'
import ActionButtons from './ActionButtons'

const DigitalObjectList = ({
  enableMove,
  enableRemove,
  identifiables,
  identifierTypes,
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
          <th className="text-center">
            <FaImage />
          </th>
          <th className="text-center">{t('label')}</th>
          <th className="text-center">{t('identifiers')}</th>
          <th className="text-center">{t('lastModified')}</th>
          <th className="text-center">{t('actions')}</th>
        </tr>
      </thead>
      <tbody>
        {identifiables.map(
          (
            {
              identifiers,
              label,
              lastModified,
              previewImage,
              previewImageRenderingHints,
              uuid,
            },
            index,
          ) => (
            <tr key={uuid}>
              <td className="text-right">
                {index + 1 + pageNumber * pageSize}
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
              <td>
                <IdentifierList
                  identifiers={identifiers}
                  identifierTypes={identifierTypes}
                />
              </td>
              <td className="text-center">
                {formatDate(lastModified, uiLocale)}
              </td>
              <td className="text-center">
                <ActionButtons
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
          ),
        )}
      </tbody>
    </Table>
  )
}

export default DigitalObjectList
