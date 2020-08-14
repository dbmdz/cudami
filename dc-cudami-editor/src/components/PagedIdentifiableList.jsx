import uniqBy from 'lodash/uniqBy'
import React, {Component} from 'react'
import {Button, Col, Label, ListGroup, ListGroupItem, Row} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaHashtag, FaImage} from 'react-icons/fa'
import ReactPaginate from 'react-paginate'

import './PagedIdentifiableList.css'
import AddAttachedIdentifiablesModal from './modals/AddAttachedIdentifiablesModal'
import RemoveAttachedIdentifiableModal from './modals/RemoveAttachedIdentifiableModal'
import IdentifiableListItem from './IdentifiableListItem'
import {
  ApiContext,
  addAttachedIdentifiables,
  loadAttachedIdentifiables,
  loadDefaultLanguage,
  removeAttachedIdentifiable,
} from '../api'
import '../polyfills'

class PagedIdentifiableList extends Component {
  pageSize = 5

  constructor(props) {
    super(props)
    this.state = {
      identifiables: [],
      modalsOpen: {
        addAttachedIdentifiables: false,
        removeAttachedIdentifiable: false,
      },
      numberOfPages: 0,
      pageNumber: 0,
    }
  }

  async componentDidMount() {
    const {apiContextPath, mockApi} = this.props
    const {content, pageSize, totalElements} = await this.loadIdentifiables()
    const defaultLanguage = await loadDefaultLanguage(apiContextPath, mockApi)
    this.setState({
      defaultLanguage,
      identifiables: content,
      numberOfPages: Math.ceil(totalElements / pageSize),
    })
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
      return console.error('an error occured')
    }
    if (identifiables.length === this.pageSize) {
      return this.updatePage({selected: pageNumber + 1})
    }
    this.updatePage({selected: pageNumber})
  }

  handleRemove = async () => {
    const {identifiables, pageNumber, removeIndex} = this.state
    const uuid = identifiables[removeIndex].uuid
    const successful = await this.removeIdentifiable(uuid)
    if (!successful) {
      return console.error('an error occured')
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

  removeIdentifiable = async (uuid) => {
    const {apiContextPath, mockApi, parentType, parentUuid, type} = this.props
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
    })
  }

  render() {
    const {
      apiContextPath,
      debug,
      mockApi,
      parentType,
      t,
      type,
      uiLocale,
    } = this.props
    return (
      <ApiContext.Provider value={{apiContextPath, mockApi}}>
        <Row>
          <Col>
            <h2>{t(`${type}s`)}</h2>
          </Col>
          <Col className="text-right">
            <Button
              onClick={() => this.toggleModal('addAttachedIdentifiables')}
            >
              {t('add')}
            </Button>
          </Col>
        </Row>
        <ListGroup className="identifiable-list">
          <ListGroupItem className="pb-0 pt-0">
            <Row className="text-center">
              <Col className="border-right pb-2 pt-2 text-right" md="1">
                <FaHashtag />
              </Col>
              <Col className="border-right pb-2 pt-2" md="1">
                <FaImage />
              </Col>
              <Col className="border-right pb-2 pt-2" md="7">
                {t('label')}
              </Col>
              <Col className="border-right pb-2 pt-2" md="2">
                {t('lastModified')}
              </Col>
              <Col className="pb-2 pt-2" md="1">
                {t('actions')}
              </Col>
            </Row>
          </ListGroupItem>
          {this.state.identifiables.map((identifiable, index) => (
            <IdentifiableListItem
              apiContextPath={apiContextPath}
              index={index + 1 + this.state.pageNumber * this.pageSize}
              key={index}
              label={
                identifiable.label[this.state.defaultLanguage] ??
                Object.values(identifiable.label)[0]
              }
              lastModified={identifiable.lastModified}
              onRemove={() => {
                this.toggleModal('removeAttachedIdentifiable')
                this.setState({removeIndex: index})
              }}
              previewImage={identifiable.previewImage}
              previewImageRenderingHints={
                identifiable.previewImageRenderingHints
              }
              type={type}
              uiLocale={uiLocale}
              uuid={identifiable.uuid}
            />
          ))}
        </ListGroup>
        {this.state.identifiables.length > 0 && (
          <ReactPaginate
            activeClassName="active"
            breakClassName="page-item"
            breakLabel="&hellip;"
            breakLinkClassName="page-link"
            containerClassName="pagination mt-2"
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
        {debug && (
          <>
            <Label className="font-weight-bold mt-3">JSON (debug)</Label>
            <pre className="border">
              <code>{JSON.stringify(this.state.identifiables, null, 4)}</code>
            </pre>
          </>
        )}
        <AddAttachedIdentifiablesModal
          defaultLanguage={this.state.defaultLanguage}
          isOpen={this.state.modalsOpen.addAttachedIdentifiables}
          onAdd={this.handleAdd}
          onToggle={() => this.toggleModal('addAttachedIdentifiables')}
          type={type}
        />
        <RemoveAttachedIdentifiableModal
          isOpen={this.state.modalsOpen.removeAttachedIdentifiable}
          onConfirm={this.handleRemove}
          onToggle={() => this.toggleModal('removeAttachedIdentifiable')}
          parentType={parentType}
          type={type}
        />
      </ApiContext.Provider>
    )
  }
}

PagedIdentifiableList.defaultProps = {
  apiContextPath: '/',
  debug: false,
  mockApi: false,
}

export default withTranslation()(PagedIdentifiableList)
