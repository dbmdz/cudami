import {useContext} from 'react'
import {useTranslation} from 'react-i18next'
import {
  Button,
  ListGroup,
  ListGroupItem,
  ListGroupItemHeading,
  ListGroupItemText,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  Spinner,
} from 'reactstrap'

import usePagination from '../../hooks/usePagination'
import AppContext from '../AppContext'
import Pagination from '../Pagination'

const SelectRenderingTemplateDialog = ({isOpen, onSelect, toggle}) => {
  const type = 'renderingTemplate'
  const {apiContextPath, defaultLanguage} = useContext(AppContext)
  const {
    content: templates,
    isLoading,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  } = usePagination(apiContextPath, type, [
    {property: 'name'},
    {property: 'uuid'},
  ])
  const {t} = useTranslation()
  if (isLoading) {
    return <Spinner color="secondary" />
  }
  return (
    <Modal isOpen={isOpen} toggle={toggle}>
      <ModalHeader toggle={toggle}>{t('chooseRenderingTemplate')}</ModalHeader>
      <ModalBody>
        <Pagination
          changePage={({selected}) => setPageNumber(selected)}
          numberOfPages={numberOfPages}
          pageNumber={pageNumber}
          totalElements={totalElements}
          type={type}
        />
        <ListGroup>
          {templates.map(({description, label, name, uuid}) => (
            <ListGroupItem key={uuid}>
              <ListGroupItemHeading>
                {label?.[defaultLanguage] ?? name}
              </ListGroupItemHeading>
              {description?.[defaultLanguage] && (
                <ListGroupItemText className="mb-2">
                  {description[defaultLanguage]}
                </ListGroupItemText>
              )}
              <Button
                color="primary"
                onClick={() => {
                  onSelect(name)
                  toggle()
                }}
                size="sm"
              >
                {t('choose')}
              </Button>
            </ListGroupItem>
          ))}
        </ListGroup>
        <Pagination
          changePage={({selected}) => setPageNumber(selected)}
          numberOfPages={numberOfPages}
          pageNumber={pageNumber}
          position="under"
          showTotalElements={false}
          totalElements={totalElements}
          type={type}
        />
      </ModalBody>
      <ModalFooter>
        <Button color="light" onClick={toggle}>
          {t('cancel')}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

export default SelectRenderingTemplateDialog
