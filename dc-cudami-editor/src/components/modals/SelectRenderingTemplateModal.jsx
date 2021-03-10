import React, {useContext} from 'react'
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
import {useTranslation} from 'react-i18next'

import AppContext from '../AppContext'
import Pagination from '../Pagination'
import usePagination from '../../hooks/usePagination'

const SelectRenderingTemplateModal = ({isOpen, onSelect, toggle}) => {
  const type = 'renderingTemplate'
  const {apiContextPath, defaultLanguage, mockApi} = useContext(AppContext)
  const {
    content: templates,
    isLoading,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  } = usePagination(apiContextPath, mockApi, type)
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
        <Button color="secondary" onClick={toggle}>
          {t('cancel')}
        </Button>
      </ModalFooter>
    </Modal>
  )
}

export default SelectRenderingTemplateModal
