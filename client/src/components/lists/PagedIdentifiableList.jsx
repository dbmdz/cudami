import '../../polyfills'

import uniqBy from 'lodash-es/uniqBy'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {Button, Card, CardBody, Col, Nav, Row} from 'reactstrap'

import {
  addAttachedObject,
  addAttachedObjects,
  findAttachedObjects,
  findRootObjects,
  getDefaultLanguage,
  removeAttachedObject,
  typeToEndpointMapping,
  updateAttachedObjectsOrder,
} from '../../api'
import AppContext from '../AppContext'
import AddAttachedIdentifiablesDialog from '../dialogs/AddAttachedIdentifiablesDialog'
import RemoveAttachedIdentifiableDialog from '../dialogs/RemoveAttachedIdentifiableDialog'
import FeedbackMessage from '../FeedbackMessage'
import LanguageTab from '../LanguageTab'
import ListSearch from '../ListSearch'
import Pagination from '../Pagination'
import {getLabelValue} from '../utils'
import ArticleList from './ArticleList'
import CollectionList from './CollectionList'
import CorporateBodyList from './CorporateBodyList'
import DigitalObjectList from './DigitalObjectList'
import EntityList from './EntityList'
import FileResourceList from './FileResourceList'
import GeoLocationList from './GeoLocationList'
import ItemList from './ItemList'
import PersonList from './PersonList'
import ProjectList from './ProjectList'
import TopicList from './TopicList'
import WebpageList from './WebpageList'
import WebsiteList from './WebsiteList'
import WorkList from './WorkList'

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
      showSearch: false,
    }
  }

  async componentDidMount() {
    const {content: identifierTypes} = await findRootObjects(
      this.props.apiContextPath,
      'identifierType',
      {
        pageNumber: 0,
        pageSize: this.pageSize,
        sorting: {
          orders: [{property: 'namespace'}, {property: 'uuid'}],
        },
      },
    )
    const defaultLanguage = await getDefaultLanguage(this.props.apiContextPath)
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0, {
      defaultLanguage,
    })
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
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0, {
      pageSize: this.state.totalElements,
    })
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
    const successful = await addAttachedObject(
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
    const successful = await addAttachedObjects(
      apiContextPath,
      identifiables,
      parentType,
      parentUuid,
      type,
    )
    return successful
  }

  executeSearch = async () => {
    const {content, pageSize, totalElements} = await this.loadIdentifiables(0)
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
      item: ItemList,
      person: PersonList,
      project: ProjectList,
      topic: TopicList,
      webpage: WebpageList,
      website: WebsiteList,
      work: WorkList,
    }
    const {enableMove, enableRemove, parentType, showEdit, type} = this.props
    const {
      activeLanguage,
      changeOfOrderActive,
      identifiables,
      identifierTypes,
      pageNumber,
    } = this.state
    const ListComponent = LIST_COMPONENT_MAPPING[type]
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
    const {parentUuid, type} = this.props
    const {
      activeLanguage,
      defaultLanguage,
      identifiables,
      pageNumber,
      removeIndex,
    } = this.state
    const {label, uuid} = identifiables[removeIndex]
    const successful = await this.removeIdentifiable(parentUuid, uuid)
    if (!successful) {
      return console.error('an error occured while removing the identifiable')
    }
    this.setState({
      feedbackMessage: {
        color: 'success',
        key: `${type}RemovedSuccessfully`,
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

  loadIdentifiables = async (
    pageNumber,
    {
      defaultLanguage = this.state.defaultLanguage,
      pageSize = this.pageSize,
    } = {},
  ) => {
    const {apiContextPath, parentType, parentUuid, type} = this.props
    const {searchTerm} = this.state
    if (parentType && parentUuid) {
      return await findAttachedObjects(
        apiContextPath,
        parentType,
        parentUuid,
        type,
        {pageNumber, pageSize, searchTerm},
      )
    }
    return await findRootObjects(apiContextPath, type, {
      pageNumber,
      pageSize,
      searchTerm,
      sorting: {
        orders: [{property: 'label', subProperty: defaultLanguage}],
      },
    })
  }

  removeIdentifiable = async (parentUuid, uuid) => {
    const {apiContextPath, parentType, type} = this.props
    const successful = await removeAttachedObject(
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
    const successful = await updateAttachedObjectsOrder(
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
              (0 as count activates it)
            */}
            {parentType ? (
              <h2>{t(`types:${type}`, {context: parentType, count: 0})}</h2>
            ) : (
              <h1>{t(`types:${type}`, {count: 0})}</h1>
            )}
          </Col>
          <Col className="text-right">
            {showNew && (
              <Button color="primary" href={createUrl}>
                {t('new')}
              </Button>
            )}
            {enableAdd && (
              <Button
                className={showNew ? 'ml-1' : ''}
                color="primary"
                onClick={() => this.toggleDialog('addAttachedIdentifiables')}
              >
                {t('add')}
              </Button>
            )}
            {showChangeOfOrder && (
              <Button
                className={showNew || enableAdd ? 'ml-1' : ''}
                color="primary"
                onClick={this.activateChangeOfOrder}
              >
                {t('changeOrder')}
              </Button>
            )}
            {changeOfOrderActive && (
              <Button
                className={showNew || enableAdd ? 'ml-1' : ''}
                color="primary"
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
                <ListSearch
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
            toggle={() => this.toggleDialog('addAttachedIdentifiables')}
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
            toggle={() => this.toggleDialog('moveAttachedIdentifiable')}
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
  enableSearch: true,
  showEdit: false,
  showNew: false,
}

export default withTranslation()(PagedIdentifiableList)
