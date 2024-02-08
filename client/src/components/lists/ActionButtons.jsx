import {useTranslation} from 'react-i18next'
import {FaExchangeAlt, FaEye, FaPencilAlt, FaUnlink} from 'react-icons/fa'
import {Button, ButtonGroup} from 'reactstrap'

const ActionButtons = ({
  children,
  editUrl,
  enableMove,
  enableRemove,
  onMove,
  onRemove,
  parentType,
  showEdit,
  showView = true,
  viewUrl,
}) => {
  const {t} = useTranslation()
  return (
    <>
      <ButtonGroup>
        {showView && (
          <Button className="p-0" color="link" href={viewUrl} title={t('view')}>
            <FaEye />
          </Button>
        )}
        {showEdit && (
          <Button
            className="ml-1 p-0"
            color="link"
            href={editUrl ?? `${viewUrl}/edit`}
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
      {children && <ButtonGroup>{children}</ButtonGroup>}
    </>
  )
}

export default ActionButtons
