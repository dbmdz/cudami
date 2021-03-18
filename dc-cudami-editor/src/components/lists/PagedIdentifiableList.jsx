import '../../polyfills'

import uniqBy from 'lodash/uniqBy'
import React, {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {Button, Card, CardBody, Col, Nav, Row} from 'reactstrap'

import {
  addAttachedIdentifiable,
  addAttachedIdentifiables,
  getIdentifierTypes,
  loadAttachedIdentifiables,
  loadDefaultLanguage,
  loadRootIdentifiables,
  removeAttachedIdentifiable,
  typeToEndpointMapping,
  updateAttachedIdentifiablesOrder,
} from '../../api'
import AppContext from '../AppContext'
import AddAttachedIdentifiablesDialog from '../dialogs/AddAttachedIdentifiablesDialog'
import RemoveAttachedIdentifiableDialog from '../dialogs/RemoveAttachedIdentifiableDialog'
import FeedbackMessage from '../FeedbackMessage'
import IdentifiableSearch from '../IdentifiableSearch'
import LanguageTab from '../LanguageTab'
import Pagination from '../Pagination'
import CollectionList from './CollectionList'
import CorporateBodyList from './CorporateBodyList'
import DigitalObjectList from './DigitalObjectList'
import GeoLocationList from './GeoLocationList'
import PersonList from './PersonList'
import ProjectList from './ProjectList'
import WebpageList from './WebpageList'
import WebsiteList from './WebsiteList'

class PagedIdentifiableList extends Component {
  pageSize = 20

  constructor(props) {
    super(props)
    const {existingLanguages} = this.props
    this.state = {
      activeLanguage: existingLanguages?.[0] ?? '',
      changeOfOrderActive: false,
      dialogsOpen: {
        addAttachedIdentifiables: false,
        moveAttachedIdentifiable: false,
        removeAttachedIdentifiable: false,
      },
      existingLanguages: existingLanguages ?? [],
      identifiables: [],
      identifierTypes: [],
      numberOfPages: 0,
      pageNumber: 0,
      totalElements: 0,
      searchTerm: '',
    }
  }

  async componentDidMount() {
    const {apiContextPath, mockApi} = this.props
    const identifierTypes = await getIdentifierTypes(apiContextPath, mockApi)
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0)
    const defaultLanguage = await loadDefaultLanguage(apiContextPath, mockApi)
    this.setState({
      defaultLanguage,
      identifiables: content,
      identifierTypes,
      numberOfPages: Math.ceil(totalElements / pageSize),
      totalElements,
    })
  }

  activateChangeOfOrder = async () => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(
      0,
      this.state.totalElements
    )
    this.setState({
      changeOfOrderActive: true,
      identifiables: content,
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

  executeSearch = async () => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(
      0,
      this.pageSize,
      this.state.searchTerm
    )
    this.setState({
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
      pageNumber: 0,
      totalElements,
    })
  }

  getLabelValue = (label) => {
    return (
      label[this.state.activeLanguage] ??
      label[this.state.defaultLanguage] ??
      Object.values(label)[0]
    )
  }

  getListComponent = () => {
    const LIST_COMPONENT_MAPPING = {
      collection: CollectionList,
      corporateBody: CorporateBodyList,
      digitalObject: DigitalObjectList,
      geoLocation: GeoLocationList,
      person: PersonList,
      project: ProjectList,
      subcollection: CollectionList,
      webpage: WebpageList,
      website: WebsiteList,
    }
    const ListComponent = LIST_COMPONENT_MAPPING[this.props.type]
    const {enableMove, enableRemove, parentType, showEdit, type} = this.props
    const {
      activeLanguage,
      changeOfOrderActive,
      identifiables,
      identifierTypes,
      pageNumber,
    } = this.state
    return (
      <ListComponent
        changeOfOrderActive={changeOfOrderActive}
        enableMove={enableMove}
        enableRemove={enableRemove}
        identifiables={identifiables}
        identifierTypes={identifierTypes}
        language={activeLanguage}
        onChangeOrder={(identifiables) => this.setState({identifiables})}
        onMove={(moveIndex) => {
          this.toggleDialog('moveAttachedIdentifiable')
          this.setState({moveIndex})
        }}
        onRemove={(removeIndex) => {
          this.toggleDialog('removeAttachedIdentifiable')
          this.setState({removeIndex})
        }}
        pageNumber={pageNumber}
        pageSize={this.pageSize}
        parentType={parentType}
        showEdit={showEdit}
        type={type}
      />
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
            links: [
              `${apiContextPath}${typeToEndpointMapping[type]}/${targetUuid}`,
            ],
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

  loadIdentifiables = async (pageNumber, pageSize = this.pageSize) => {
    const {apiContextPath, mockApi, parentType, parentUuid, type} = this.props
    if (parentType && parentUuid) {
      return await loadAttachedIdentifiables(
        apiContextPath,
        mockApi,
        parentType,
        parentUuid,
        type,
        pageNumber,
        pageSize,
        this.state.searchTerm
      )
    }
    return await loadRootIdentifiables(
      apiContextPath,
      mockApi,
      type,
      pageNumber,
      pageSize,
      this.state.searchTerm
    )
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

  saveChangeOfOrder = async () => {
    const {apiContextPath, mockApi, parentType, parentUuid, type} = this.props
    const successful = await updateAttachedIdentifiablesOrder(
      apiContextPath,
      mockApi,
      this.state.identifiables,
      parentType,
      parentUuid,
      type
    )
    if (!successful) {
      return this.setState({
        feedbackMessage: {
          color: 'danger',
          key: 'orderNotChangedSuccessfully',
        },
      })
    }
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0)
    this.setState({
      changeOfOrderActive: false,
      feedbackMessage: {
        color: 'success',
        key: 'orderChangedSuccessfully',
      },
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
      totalElements,
    })
  }

  toggleDialog = (name) => {
    this.setState({
      dialogsOpen: {
        ...this.state.dialogsOpen,
        [name]: !this.state.dialogsOpen[name],
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
      enableChangeOfOrder,
      enableMove,
      enableRemove,
      enableSearch,
      mockApi,
      parentType,
      parentUuid,
      showNew,
      t,
      type,
      uiLocale,
    } = this.props
    const {
      activeLanguage,
      changeOfOrderActive,
      defaultLanguage,
      existingLanguages,
      feedbackMessage,
      identifierTypes,
      dialogsOpen,
      numberOfPages,
      pageNumber,
      searchTerm,
      totalElements,
    } = this.state
    const showChangeOfOrder =
      enableChangeOfOrder && !changeOfOrderActive && totalElements > 1
    let createUrl = `${apiContextPath}${typeToEndpointMapping[type]}/new`
    if (parentType && parentUuid) {
      createUrl = `${createUrl}?parentType=${parentType}&parentUuid=${parentUuid}`
    }
    return (
      <AppContext.Provider
        value={{apiContextPath, defaultLanguage, mockApi, uiLocale}}
      >
        <Row>
          <Col>
            {parentType ? (
              <h2>{t(`${type}s`, {context: parentType})}</h2>
            ) : (
              <h1>{t(`${type}s`)}</h1>
            )}
          </Col>
          <Col className="text-right">
            {showNew && <Button href={createUrl}>{t('new')}</Button>}
            {enableAdd && (
              <Button
                className={showNew ? 'ml-1' : ''}
                onClick={() => this.toggleDialog('addAttachedIdentifiables')}
              >
                {t('add')}
              </Button>
            )}
          </Col>
        </Row>
        {!parentType && <hr />}
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
            <div className="d-flex justify-content-between">
              <Pagination
                changePage={this.updatePage}
                numberOfPages={numberOfPages}
                pageNumber={pageNumber}
                totalElements={totalElements}
                type={type}
              />
              {showChangeOfOrder && (
                <Button className="mb-2" onClick={this.activateChangeOfOrder}>
                  {t('changeOrder')}
                </Button>
              )}
              {changeOfOrderActive && (
                <Button className="mb-2" onClick={this.saveChangeOfOrder}>
                  {t('save')}
                </Button>
              )}
              {enableSearch && (
                <IdentifiableSearch
                  onChange={(value) => this.setState({searchTerm: value})}
                  onSubmit={this.executeSearch}
                  value={searchTerm}
                />
              )}
            </div>
            {this.getListComponent()}
            <Pagination
              changePage={this.updatePage}
              numberOfPages={numberOfPages}
              pageNumber={pageNumber}
              position="under"
              showTotalElements={false}
              totalElements={totalElements}
              type={type}
            />
          </CardBody>
        </Card>
        {enableAdd && (
          <AddAttachedIdentifiablesDialog
            action="add"
            identifierTypes={identifierTypes}
            isOpen={dialogsOpen.addAttachedIdentifiables}
            onSubmit={this.handleAdd}
            onToggle={() => this.toggleDialog('addAttachedIdentifiables')}
            type={type}
          />
        )}
        {enableMove && (
          <AddAttachedIdentifiablesDialog
            action="move"
            identifierTypes={identifierTypes}
            isOpen={dialogsOpen.moveAttachedIdentifiable}
            maxElements={1}
            onSubmit={(identifiables) => this.handleMove(identifiables[0])}
            onToggle={() => this.toggleDialog('moveAttachedIdentifiable')}
            type={type}
          />
        )}
        {enableRemove && (
          <RemoveAttachedIdentifiableDialog
            isOpen={dialogsOpen.removeAttachedIdentifiable}
            onConfirm={this.handleRemove}
            onToggle={() => this.toggleDialog('removeAttachedIdentifiable')}
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
  enableChangeOfOrder: false,
  enableMove: false,
  enableRemove: false,
  enableSearch: false,
  mockApi: false,
  showEdit: false,
  showNew: false,
}

export default withTranslation()(PagedIdentifiableList)
