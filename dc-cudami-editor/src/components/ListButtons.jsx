import React from 'react'
import {Button, ButtonGroup} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaExchangeAlt, FaEye, FaPencilAlt, FaUnlink} from 'react-icons/fa'

const ListButtons = ({
  enableMove,
  enableRemove,
  onMove,
  onRemove,
  parentType,
  showEdit,
  viewUrl,
}) => {
  const {t} = useTranslation()
  return (
    <>
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
    </>
  )
}

export default ListButtons
