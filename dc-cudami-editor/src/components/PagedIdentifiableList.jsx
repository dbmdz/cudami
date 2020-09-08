import classNames from 'classnames'
import uniqBy from 'lodash/uniqBy'
import React, {Component} from 'react'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaHashtag, FaImage} from 'react-icons/fa'
import ReactPaginate from 'react-paginate'

import './common.css'
import AppContext from './AppContext'
import FeedbackMessage from './FeedbackMessage'
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

  getLabelValue = (label) => {
    return (
      label[this.state.activeLanguage] ??
      label[this.state.defaultLanguage] ??
      Object.values(label)[0]
    )
  }

  handleAdd = async (identifiablesToAdd) => {
    const {identifiables, pageNumber} = this.state
    const uniqueIdentifiables = uniqBy(identifiablesToAdd, 'uuid')
    const successful = await this.addIdentifiables(uniqueIdentifiables)
    if (!successful) {
      return console.error('an error occured while adding the identifiables')
    }
    this.setState({
      feedbackMessage: {
        color: 'success',
        key: `${this.props.type}AddedSuccessfully`,
        values: {count: uniqueIdentifiables.length},
      },
    })
    if (identifiables.length === this.pageSize) {
      return this.updatePage({selected: pageNumber + 1})
    }
    this.updatePage({selected: pageNumber})
  }

  handleMove = async ({label: targetLabel, uuid: targetUuid}) => {
    const {apiContextPath, parentUuid, type} = this.props
    const {identifiables, moveIndex, pageNumber} = this.state
    const {label, uuid} = identifiables[moveIndex]
    if (uuid === targetUuid) {
      return console.error('an identifiable cannot be moved to itself')
    }
    const addedSuccessfully = await this.addIdentifiable(targetUuid, uuid)
    if (addedSuccessfully) {
      const removedSuccessfully = await this.removeIdentifiable(
        parentUuid,
        uuid
      )
      if (removedSuccessfully) {
        this.setState({
          feedbackMessage: {
            color: 'success',
            key: `${type}MovedSuccessfully`,
            links: [`${apiContextPath}${type.toLowerCase()}s/${targetUuid}`],
            values: {
              name: this.getLabelValue(label),
              targetName: this.getLabelValue(targetLabel),
            },
          },
        })
        if (pageNumber > 0 && identifiables.length === 1) {
          return this.updatePage({selected: pageNumber - 1})
        }
        this.updatePage({selected: pageNumber})
      }
    }
  }

  handleRemove = async () => {
    const {identifiables, pageNumber, removeIndex} = this.state
    const {label, uuid} = identifiables[removeIndex]
    const successful = await this.removeIdentifiable(
      this.props.parentUuid,
      uuid
    )
    if (!successful) {
      return console.error('an error occured while removing the identifiable')
    }
    this.setState({
      feedbackMessage: {
        color: 'success',
        key: `${this.props.type}RemovedSuccessfully`,
        values: {
          name: this.getLabelValue(label),
        },
      },
    })
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
      feedbackMessage,
      identifiables,
      identifierTypes,
      modalsOpen,
      numberOfPages,
      pageNumber,
      totalElements,
    } = this.state
    const TablePagination = ({position, showTotalElements}) => (
      <>
        <ReactPaginate
          activeClassName="active"
          breakClassName="page-item"
          breakLabel="&hellip;"
          breakLinkClassName="page-link"
          containerClassName={classNames({
            'd-inline-flex': true,
            pagination: true,
            'mb-0': position === 'under',
            'mb-2': position === 'above',
            'mt-2': position === 'under',
          })}
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
        {showTotalElements && (
          <span className="ml-2">
            {t(`totalElements.${type}s`, {count: totalElements})}
          </span>
        )}
      </>
    )
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
        {feedbackMessage && (
          <FeedbackMessage
            className="mb-2"
            message={feedbackMessage}
            onClose={() => this.setState({feedbackMessage: undefined})}
          />
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
            {identifiables.length > 0 && (
              <TablePagination position="above" showTotalElements />
            )}
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
                    enableMove={enableMove}
                    enableRemove={enableRemove}
                    identifiable={identifiable}
                    identifierTypes={identifierTypes}
                    index={index + 1 + pageNumber * this.pageSize}
                    key={identifiable.uuid}
                    language={activeLanguage}
                    onMove={() => {
                      this.toggleModal('moveAttachedIdentifiable')
                      this.setState({moveIndex: index})
                    }}
                    onRemove={() => {
                      this.toggleModal('removeAttachedIdentifiable')
                      this.setState({removeIndex: index})
                    }}
                    parentType={parentType}
                    showEdit={showEdit}
                    type={type}
                    uiLocale={uiLocale}
                  />
                ))}
              </tbody>
            </Table>
            {identifiables.length > 0 && <TablePagination position="under" />}
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
