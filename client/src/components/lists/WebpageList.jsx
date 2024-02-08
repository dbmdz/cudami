import {forwardRef, useContext, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {
  FaArrowsAltV,
  FaCheck,
  FaHashtag,
  FaImage,
  FaTimes,
} from 'react-icons/fa'
import {List, arrayMove} from 'react-movable'
import {Button, Table} from 'reactstrap'

import {typeToEndpointMapping} from '../../api'
import AppContext from '../AppContext'
import PreviewImage from '../PreviewImage'
import PublicationStatus from '../PublicationStatus'
import {formatDate} from '../utils'
import ActionButtons from './ActionButtons'

const WebpageItem = forwardRef(
  (
    {
      changeOfOrderActive,
      enableMove,
      enableRemove,
      identifiable,
      index,
      language,
      onMove,
      onRemove,
      pageNumber,
      pageSize,
      parentType,
      showEdit,
      style = {},
      type,
      widths,
    },
    ref,
  ) => {
    const {
      label,
      lastModified,
      previewImage,
      previewImageRenderingHints,
      publicationEnd,
      publicationStart,
      renderingHints,
      uuid,
    } = identifiable
    const {apiContextPath, uiLocale} = useContext(AppContext)
    const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
    const [
      indexCol,
      publicationStatusCol,
      previewCol,
      labelCol,
      publicationStartCol,
      publicationEndCol,
      lastModifiedCol,
      actionsCol,
    ] = widths
    return (
      <tr key={uuid} ref={ref} style={style}>
        <td className="text-right" style={{width: indexCol}}>
          {changeOfOrderActive ? (
            <Button className="p-0" color="link" data-movable-handle size="sm">
              <FaArrowsAltV />
            </Button>
          ) : isNaN(index) ? (
            ''
          ) : (
            index + 1 + pageNumber * pageSize
          )}
        </td>
        <td className="text-center" style={{width: publicationStatusCol}}>
          <PublicationStatus
            publicationEnd={publicationEnd}
            publicationStart={publicationStart}
          />
        </td>
        <td className="text-center">
          {!renderingHints || renderingHints.showInPageNavigation ? (
            <FaCheck className="text-success" />
          ) : (
            <FaTimes className="text-danger" />
          )}
        </td>
        <td className="text-center" style={{width: previewCol}}>
          <PreviewImage
            image={previewImage}
            renderingHints={previewImageRenderingHints}
            width={30}
          />
        </td>
        <td style={{width: labelCol}}>
          {label[language] && (
            <a href={`${viewBaseUrl}/${uuid}`}>{label[language]}</a>
          )}
        </td>
        <td style={{width: publicationStartCol}}>
          {formatDate(publicationStart, uiLocale, true)}
        </td>
        <td style={{width: publicationEndCol}}>
          {formatDate(publicationEnd, uiLocale, true)}
        </td>
        <td className="text-center" style={{width: lastModifiedCol}}>
          {formatDate(lastModified, uiLocale)}
        </td>
        <td className="text-center" style={{width: actionsCol}}>
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
    )
  },
)

WebpageItem.displayName = 'WebpageItem'

const WebpageList = ({
  changeOfOrderActive,
  identifiables,
  onChangeOrder,
  ...rest
}) => {
  const [widths, setWidths] = useState([])
  const {t} = useTranslation()
  return (
    <List
      beforeDrag={({elements, index}) => {
        const cells = Array.from(elements[index].children)
        const widths = cells.map((cell) => window.getComputedStyle(cell).width)
        setWidths(widths)
      }}
      lockVertically
      onChange={({newIndex, oldIndex}) =>
        onChangeOrder(arrayMove(identifiables, oldIndex, newIndex))
      }
      renderItem={({isDragged, props, value}) => {
        const _widths = isDragged ? widths : []
        const item = (
          <WebpageItem
            changeOfOrderActive={changeOfOrderActive}
            identifiable={value}
            index={props.key}
            style={{visibility: isDragged && 'hidden'}}
            widths={_widths}
            {...props}
            {...rest}
          />
        )
        return isDragged ? (
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <tbody>{item}</tbody>
          </Table>
        ) : (
          item
        )
      }}
      renderList={({children, props}) => (
        <Table bordered className="mb-0" hover responsive size="sm" striped>
          <thead>
            <tr>
              <th className="text-right">
                <FaHashtag />
              </th>
              <th className="text-center">{t('status')}</th>
              <th className="text-center">{t('inPageNavigation')}</th>
              <th className="text-center">
                <FaImage />
              </th>
              <th className="text-center">{t('label')}</th>
              <th className="text-center">
                {t('publicationStatus:startDate')}
              </th>
              <th className="text-center">{t('publicationStatus:endDate')}</th>
              <th className="text-center">{t('lastModified')}</th>
              <th className="text-center">{t('actions')}</th>
            </tr>
          </thead>
          <tbody {...props}>{children}</tbody>
        </Table>
      )}
      values={
        changeOfOrderActive
          ? identifiables
          : identifiables.map((o) => ({...o, disabled: true}))
      }
    />
  )
}

export default WebpageList
