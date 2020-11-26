import React, {forwardRef, useContext, useState} from 'react'
import {Button, Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaArrowsAltV, FaHashtag, FaImage} from 'react-icons/fa'
import {List, arrayMove} from 'react-movable'

import AppContext from './AppContext'
import ListButtons from './ListButtons'
import PreviewImage from './PreviewImage'
import PublicationStatus from './PublicationStatus'
import {formatDate} from './utils'
import {typeToEndpointMapping} from '../api'

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
    ref
  ) => {
    const {
      label,
      lastModified,
      previewImage,
      previewImageRenderingHints,
      publicationEnd,
      publicationStart,
      uuid,
    } = identifiable
    const {apiContextPath, uiLocale} = useContext(AppContext)
    const viewBaseUrl = `${apiContextPath}${typeToEndpointMapping[type]}`
    const publicationEndDate = publicationEnd && new Date(publicationEnd)
    const publicationStartDate = publicationStart && new Date(publicationStart)
    return (
      <tr key={uuid} ref={ref} style={style}>
        <td className="text-right" style={{width: widths[0]}}>
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
        <td className="text-center" style={{width: widths[1]}}>
          <PublicationStatus
            publicationEnd={publicationEndDate}
            publicationStart={publicationStartDate}
          />
        </td>
        <td className="text-center" style={{width: widths[2]}}>
          <PreviewImage
            image={previewImage}
            renderingHints={previewImageRenderingHints}
            width={30}
          />
        </td>
        <td style={{width: widths[3]}}>
          {label[language] && (
            <a href={`${viewBaseUrl}/${uuid}`}>{label[language]}</a>
          )}
        </td>
        <td style={{width: widths[4]}}>
          {formatDate(publicationStartDate, uiLocale, true)}
        </td>
        <td style={{width: widths[5]}}>
          {formatDate(publicationEndDate, uiLocale, true)}
        </td>
        <td className="text-center" style={{width: widths[6]}}>
          {formatDate(new Date(lastModified), uiLocale)}
        </td>
        <td className="text-center" style={{width: widths[7]}}>
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
)

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
              <th className="text-center">
                <FaImage />
              </th>
              <th className="text-center">{t('label')}</th>
              <th className="text-center">
                {t('publicationStatus.startDate')}
              </th>
              <th className="text-center">{t('publicationStatus.endDate')}</th>
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
