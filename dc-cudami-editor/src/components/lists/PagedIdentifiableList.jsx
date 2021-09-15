import '../../polyfills'

import uniqBy from 'lodash/uniqBy'
import {Component} from 'react'
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
import {getLabelValue} from '../utils'
import ArticleList from './ArticleList'
import CollectionList from './CollectionList'
import CorporateBodyList from './CorporateBodyList'
import DigitalObjectList from './DigitalObjectList'
import EntityList from './EntityList'
import FileResourceList from './FileResourceList'
import GeoLocationList from './GeoLocationList'
import PersonList from './PersonList'
import ProjectList from './ProjectList'
import TopicList from './TopicList'
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
      showSearch: true,
    }
  }

  async componentDidMount() {
    const identifierTypes = await getIdentifierTypes(this.props.apiContextPath)
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0)
    const defaultLanguage = await loadDefaultLanguage(this.props.apiContextPath)
    this.setState({
      defaultLanguage,
      identifiables: content,
      identifierTypes,
      numberOfPages: Math.ceil(totalElements / pageSize),
      totalElements,
      showSearch: totalElements > 0,
    })
  }

  activateChangeOfOrder = async () => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(
      0,
      this.state.totalElements,
    )
    this.setState({
      changeOfOrderActive: true,
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
      showSearch: false,
      totalElements,
    })
  }

  addIdentifiable = async (parentUuid, uuid) => {
    const {apiContextPath, parentType, type} = this.props
    const successful = await addAttachedIdentifiable(
      apiContextPath,
      parentType,
      parentUuid,
      type,
      uuid,
    )
    return successful
  }

  addIdentifiables = async (identifiables) => {
    const {apiContextPath, parentType, parentUuid, type} = this.props
    const successful = await addAttachedIdentifiables(
      apiContextPath,
      identifiables,
      parentType,
      parentUuid,
      type,
    )
    return successful
  }

  executeSearch = async () => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(
      0,
      this.pageSize,
      this.state.searchTerm,
    )
    this.setState({
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
      pageNumber: 0,
      totalElements,
    })
  }

  getListComponent = () => {
    const LIST_COMPONENT_MAPPING = {
      article: ArticleList,
      collection: CollectionList,
      corporateBody: CorporateBodyList,
      digitalObject: DigitalObjectList,
      entity: EntityList,
      fileResource: FileResourceList,
      geoLocation: GeoLocationList,
      person: PersonList,
      project: ProjectList,
      subcollection: CollectionList,
      subtopic: TopicList,
      topic: TopicList,
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
    const {
      activeLanguage,
      defaultLanguage,
      identifiables,
      moveIndex,
      pageNumber,
    } = this.state
    const {label, uuid} = identifiables[moveIndex]
    if (uuid === targetUuid) {
      return console.error('an identifiable cannot be moved to itself')
    }
    const addedSuccessfully = await this.addIdentifiable(targetUuid, uuid)
    if (addedSuccessfully) {
      const removedSuccessfully = await this.removeIdentifiable(
        parentUuid,
        uuid,
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
              name: getLabelValue(label, activeLanguage, defaultLanguage),
              targetName: getLabelValue(
                targetLabel,
                activeLanguage,
                defaultLanguage,
              ),
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
    const {
      activeLanguage,
      defaultLanguage,
      identifiables,
      pageNumber,
      removeIndex,
    } = this.state
    const {label, uuid} = identifiables[removeIndex]
    const successful = await this.removeIdentifiable(
      this.props.parentUuid,
      uuid,
    )
    if (!successful) {
      return console.error('an error occured while removing the identifiable')
    }
    this.setState({
      feedbackMessage: {
        color: 'success',
        key: `${this.props.type}RemovedSuccessfully`,
        values: {
          name: getLabelValue(label, activeLanguage, defaultLanguage),
        },
      },
    })
    if (pageNumber > 0 && identifiables.length === 1) {
      return this.updatePage({selected: pageNumber - 1})
    }
    this.updatePage({selected: pageNumber})
  }

  loadIdentifiables = async (pageNumber, pageSize = this.pageSize) => {
    const {apiContextPath, parentType, parentUuid, type} = this.props
    if (parentType && parentUuid) {
      return await loadAttachedIdentifiables(
        apiContextPath,
        parentType,
        parentUuid,
        type,
        pageNumber,
        pageSize,
        this.state.searchTerm,
      )
    }
    return await loadRootIdentifiables(
      apiContextPath,
      type,
      pageNumber,
      pageSize,
      this.state.searchTerm,
    )
  }

  removeIdentifiable = async (parentUuid, uuid) => {
    const {apiContextPath, parentType, type} = this.props
    const successful = await removeAttachedIdentifiable(
      apiContextPath,
      parentType,
      parentUuid,
      type,
      uuid,
    )
    return successful
  }

  saveChangeOfOrder = async () => {
    const {apiContextPath, parentType, parentUuid, type} = this.props
    const successful = await updateAttachedIdentifiablesOrder(
      apiContextPath,
      this.state.identifiables,
      parentType,
      parentUuid,
      type,
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
      showSearch: true,
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
      selected,
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
      showSearch,
    } = this.state
    const showChangeOfOrder =
      enableChangeOfOrder && !changeOfOrderActive && totalElements > 1
    let createUrl = `${apiContextPath}${typeToEndpointMapping[type]}/new`
    if (parentType && parentUuid) {
      createUrl = `${createUrl}?parentType=${parentType}&parentUuid=${parentUuid}`
    }
    return (
      <AppContext.Provider value={{apiContextPath, defaultLanguage, uiLocale}}>
        <Row>
          <Col>
            {/*
              We want to force the usage of the plural form here
              (0 as count also activates it)
            */}
            {parentType ? (
              <h2>{t(`types:${type}`, {context: parentType, count: 0})}</h2>
            ) : (
              <h1>{t(`types:${type}_plural`)}</h1>
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
            {showChangeOfOrder && (
              <Button
                className={showNew || enableAdd ? 'ml-1' : ''}
                onClick={this.activateChangeOfOrder}
              >
                {t('changeOrder')}
              </Button>
            )}
            {changeOfOrderActive && (
              <Button
                className={showNew || enableAdd ? 'ml-1' : ''}
                onClick={this.saveChangeOfOrder}
              >
                {t('save')}
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
              {enableSearch && showSearch && (
                <IdentifiableSearch
                  isHighlighted={totalElements === 0 && searchTerm}
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
            activeLanguage={activeLanguage}
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
            activeLanguage={activeLanguage}
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
  showEdit: false,
  showNew: false,
}

export default withTranslation()(PagedIdentifiableList)
