import uniqBy from 'lodash/uniqBy'
import React, {Component} from 'react'
import {Alert, Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaHashtag, FaImage} from 'react-icons/fa'
import ReactPaginate from 'react-paginate'

import './common.css'
import AppContext from './AppContext'
import IdentifiableListItem from './IdentifiableListItem'
import LanguageTab from './LanguageTab'
import AddAttachedIdentifiablesModal from './modals/AddAttachedIdentifiablesModal'
import RemoveAttachedIdentifiableModal from './modals/RemoveAttachedIdentifiableModal'
import {
  addAttachedIdentifiable,
  addAttachedIdentifiables,
  getIdentifierTypes,
  loadAttachedIdentifiables,
  loadDefaultLanguage,
  removeAttachedIdentifiable,
} from '../api'
import '../polyfills'

class PagedIdentifiableList extends Component {
  pageSize = 20

  constructor(props) {
    super(props)
    const {existingLanguages} = this.props
    this.state = {
      activeLanguage: existingLanguages?.[0] ?? '',
      existingLanguages: existingLanguages ?? [],
      identifiables: [],
      identifierTypes: [],
      modalsOpen: {
        addAttachedIdentifiables: false,
        moveAttachedIdentifiable: false,
        removeAttachedIdentifiable: false,
      },
      numberOfPages: 0,
      pageNumber: 0,
      showSuccessfullyMoved: false,
      totalElements: 0,
    }
  }

  async componentDidMount() {
    const {apiContextPath, mockApi} = this.props
    const identifierTypes = await getIdentifierTypes(apiContextPath, mockApi)
    const {content, pageSize, totalElements} = await this.loadIdentifiables()
    const defaultLanguage = await loadDefaultLanguage(apiContextPath, mockApi)
    this.setState({
      defaultLanguage,
      identifiables: content,
      identifierTypes,
      numberOfPages: Math.ceil(totalElements / pageSize),
      totalElements,
    })
  }

  addIdentifiable = async (parentUuid, uuid) => {
    const {apiContextPath, mockApi, parentType, type} = this.props
    const successful = await addAttachedIdentifiable(
      apiContextPath,
      mockApi,
      parentType,
      parentUuid,
      type,
      uuid
    )
    return successful
  }

  addIdentifiables = async (identifiables) => {
    const {apiContextPath, mockApi, parentType, parentUuid, type} = this.props
    const successful = await addAttachedIdentifiables(
      apiContextPath,
      mockApi,
      identifiables,
      parentType,
      parentUuid,
      type
    )
    return successful
  }

  handleAdd = async (identifiablesToAdd) => {
    const {identifiables, pageNumber} = this.state
    const uniqueIdentifiables = uniqBy(identifiablesToAdd, 'uuid')
    const successful = await this.addIdentifiables(uniqueIdentifiables)
    if (!successful) {
      return console.error('an error occured while adding the identifiables')
    }
    if (identifiables.length === this.pageSize) {
      return this.updatePage({selected: pageNumber + 1})
    }
    this.updatePage({selected: pageNumber})
  }

  handleMove = async ({uuid}) => {
    const {identifiables, moveIndex, pageNumber} = this.state
    const uuidToMove = identifiables[moveIndex].uuid
    if (uuid === uuidToMove) {
      return console.error('an identifiable cannot be moved to itself')
    }
    const addedSuccessfully = await this.addIdentifiable(uuid, uuidToMove)
    if (addedSuccessfully) {
      const removedSuccessfully = await this.removeIdentifiable(
        this.props.parentUuid,
        uuidToMove
      )
      if (removedSuccessfully) {
        this.setState({showSuccessfullyMoved: true})
        setTimeout(() => this.setState({showSuccessfullyMoved: false}), 3000)
        if (pageNumber > 0 && identifiables.length === 1) {
          return this.updatePage({selected: pageNumber - 1})
        }
        this.updatePage({selected: pageNumber})
      }
    }
  }

  handleRemove = async () => {
    const {identifiables, pageNumber, removeIndex} = this.state
    const uuidToRemove = identifiables[removeIndex].uuid
    const successful = await this.removeIdentifiable(
      this.props.parentUuid,
      uuidToRemove
    )
    if (!successful) {
      return console.error('an error occured while removing the identifiable')
    }
    if (pageNumber > 0 && identifiables.length === 1) {
      return this.updatePage({selected: pageNumber - 1})
    }
    this.updatePage({selected: pageNumber})
  }

  loadIdentifiables = async (pageNumber = 0) => {
    const {apiContextPath, mockApi, parentType, parentUuid, type} = this.props
    const identifiables = await loadAttachedIdentifiables(
      apiContextPath,
      mockApi,
      parentType,
      parentUuid,
      type,
      pageNumber,
      this.pageSize
    )
    return identifiables
  }

  removeIdentifiable = async (parentUuid, uuid) => {
    const {apiContextPath, mockApi, parentType, type} = this.props
    const successful = await removeAttachedIdentifiable(
      apiContextPath,
      mockApi,
      parentType,
      parentUuid,
      type,
      uuid
    )
    return successful
  }

  toggleModal = (name) => {
    this.setState({
      modalsOpen: {
        ...this.state.modalsOpen,
        [name]: !this.state.modalsOpen[name],
      },
    })
  }

  updatePage = async ({selected}) => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(
      selected
    )
    this.setState({
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
      pageNumber: selected,
      totalElements,
    })
  }

  render() {
    const {
      apiContextPath,
      enableAdd,
      enableMove,
      enableRemove,
      mockApi,
      parentType,
      parentUuid,
      showEdit,
      showNew,
      t,
      type,
      uiLocale,
    } = this.props
    const {
      activeLanguage,
      defaultLanguage,
      existingLanguages,
      identifiables,
      identifierTypes,
      modalsOpen,
      numberOfPages,
      pageNumber,
      showSuccessfullyMoved,
    } = this.state
    return (
      <AppContext.Provider value={{apiContextPath, defaultLanguage, mockApi}}>
        <Row>
          <Col>
            <h2>{t(`${type}s`)}</h2>
          </Col>
          <Col className="text-right">
            {showNew && (
              <Button
                href={`${apiContextPath}${type.toLowerCase()}s/new?parentType=${parentType}&parentUuid=${parentUuid}`}
              >
                {t('new')}
              </Button>
            )}
            {enableAdd && (
              <Button
                className={showNew ? 'ml-1' : ''}
                onClick={() => this.toggleModal('addAttachedIdentifiables')}
              >
                {t('add')}
              </Button>
            )}
          </Col>
        </Row>
        {showSuccessfullyMoved && (
          <Alert className="mb-2" color="info">
            {t(`${type}SuccessfullyMoved`)}
          </Alert>
        )}
        <Nav tabs>
          {existingLanguages.length > 1 &&
            existingLanguages.map((language) => (
              <LanguageTab
                activeLanguage={activeLanguage}
                key={language}
                language={language}
                toggle={(activeLanguage) => this.setState({activeLanguage})}
              />
            ))}
        </Nav>
        <Card className="border-top-0">
          <CardBody>
            {this.state.identifiables.length > 0 && (
              <ReactPaginate
                activeClassName="active"
                breakClassName="page-item"
                breakLabel="&hellip;"
                breakLinkClassName="page-link"
                containerClassName="d-inline-flex mb-2 pagination"
                disabledClassName="disabled"
                forcePage={this.state.pageNumber}
                marginPagesDisplayed={1}
                nextClassName="page-item"
                nextLabel="&raquo;"
                nextLinkClassName="page-link"
                onPageChange={this.updatePage}
                pageClassName="page-item"
                pageCount={this.state.numberOfPages}
                pageLinkClassName="page-link"
                pageRangeDisplayed={5}
                previousClassName="page-item"
                previousLabel="&laquo;"
                previousLinkClassName="page-link"
              />
            )}
            <span className="ml-2">
              {t(`totalElements.${type}s`, {count: this.state.totalElements})}
            </span>
            <Table bordered className="mb-0" hover responsive size="sm" striped>
              <thead>
                <tr>
                  <th className="text-right">
                    <FaHashtag />
                  </th>
                  <th className="text-center">
                    <FaImage />
                  </th>
                  <th>{t('label')}</th>
                  <th>{t('identifiers')}</th>
                  <th className="text-center">{t('lastModified')}</th>
                  <th className="text-center">{t('actions')}</th>
                </tr>
              </thead>
              <tbody>
                {identifiables.map((identifiable, index) => (
                  <IdentifiableListItem
                    apiContextPath={apiContextPath}
                    enableMove={enableMove}
                    enableRemove={enableRemove}
                    identifiers={identifiable.identifiers}
                    identifierTypes={identifierTypes}
                    index={index + 1 + pageNumber * this.pageSize}
                    key={identifiable.uuid}
                    label={identifiable.label[activeLanguage]}
                    lastModified={identifiable.lastModified}
                    onMove={() => {
                      this.toggleModal('moveAttachedIdentifiable')
                      this.setState({moveIndex: index})
                    }}
                    onRemove={() => {
                      this.toggleModal('removeAttachedIdentifiable')
                      this.setState({removeIndex: index})
                    }}
                    parentType={parentType}
                    previewImage={identifiable.previewImage}
                    previewImageRenderingHints={
                      identifiable.previewImageRenderingHints
                    }
                    showEdit={showEdit}
                    type={type}
                    uiLocale={uiLocale}
                    uuid={identifiable.uuid}
                  />
                ))}
              </tbody>
            </Table>
            {identifiables.length > 0 && (
              <ReactPaginate
                activeClassName="active"
                breakClassName="page-item"
                breakLabel="&hellip;"
                breakLinkClassName="page-link"
                containerClassName="mb-0 mt-2 pagination"
                disabledClassName="disabled"
                forcePage={pageNumber}
                marginPagesDisplayed={1}
                nextClassName="page-item"
                nextLabel="&raquo;"
                nextLinkClassName="page-link"
                onPageChange={this.updatePage}
                pageClassName="page-item"
                pageCount={numberOfPages}
                pageLinkClassName="page-link"
                pageRangeDisplayed={5}
                previousClassName="page-item"
                previousLabel="&laquo;"
                previousLinkClassName="page-link"
              />
            )}
          </CardBody>
        </Card>
        {enableAdd && (
          <AddAttachedIdentifiablesModal
            action="add"
            identifierTypes={identifierTypes}
            isOpen={modalsOpen.addAttachedIdentifiables}
            onSubmit={this.handleAdd}
            onToggle={() => this.toggleModal('addAttachedIdentifiables')}
            type={type}
          />
        )}
        {enableMove && (
          <AddAttachedIdentifiablesModal
            action="move"
            identifierTypes={identifierTypes}
            isOpen={modalsOpen.moveAttachedIdentifiable}
            maxElements={1}
            onSubmit={(identifiables) => this.handleMove(identifiables[0])}
            onToggle={() => this.toggleModal('moveAttachedIdentifiable')}
            type={type}
          />
        )}
        {enableRemove && (
          <RemoveAttachedIdentifiableModal
            isOpen={modalsOpen.removeAttachedIdentifiable}
            onConfirm={this.handleRemove}
            onToggle={() => this.toggleModal('removeAttachedIdentifiable')}
            parentType={parentType}
            type={type}
          />
        )}
      </AppContext.Provider>
    )
  }
}

PagedIdentifiableList.defaultProps = {
  apiContextPath: '/',
  enableAdd: false,
  enableMove: false,
  enableRemove: false,
  mockApi: false,
  showEdit: false,
  showNew: false,
}

export default withTranslation()(PagedIdentifiableList)
