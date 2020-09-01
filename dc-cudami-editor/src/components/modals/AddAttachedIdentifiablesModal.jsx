import startCase from 'lodash/startCase'
import React, {Component} from 'react'
import {
  Alert,
  Button,
  Col,
  Form,
  FormGroup,
  Input,
  ListGroup,
  ListGroupItem,
  Modal,
  ModalBody,
  ModalHeader,
  Row,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaTrash} from 'react-icons/fa'

import Autocomplete from '../Autocomplete'
import IdentifierSearch from '../IdentifierSearch'
import {getImageUrl} from '../utils'
import {ApiContext, getIdentifierTypes, searchIdentifiables} from '../../api'

class AddAttachedIdentifiablesModal extends Component {
  fixedOptions = ['label']

  constructor(props) {
    super(props)
    this.state = {
      identifiables: [],
      identifierTypes: [],
      selectedOption: 0,
    }
  }

  async componentDidMount() {
    const identifierTypes = await getIdentifierTypes(
      this.context.apiContextPath,
      this.context.mockApi
    )
    this.setState({
      identifierTypes,
    })
  }

  addIdentifiableToList = (identifiable) => {
    this.setState({
      identifiables: [...this.state.identifiables, identifiable],
    })
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      identifiables: [],
      selectedOption: 0,
    })
  }

  removeIdentifiableFromList = (index) => {
    return [
      ...this.state.identifiables.slice(0, index),
      ...this.state.identifiables.slice(index + 1),
    ]
  }

  render() {
    const {
      action,
      defaultLanguage,
      isOpen,
      maxElements,
      onSubmit,
      t,
      type,
    } = this.props
    const {identifiables, identifierTypes, selectedOption} = this.state
    const previewImageWidth = 50
    const showAutocomplete = selectedOption < this.fixedOptions.length
    const showInputFields =
      maxElements === undefined || identifiables.length < maxElements
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {t(`${action}${startCase(type).replace(' ', '')}s`)}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              onSubmit(identifiables)
              this.destroy()
            }}
          >
            {showInputFields && (
              <>
                <FormGroup className="d-inline-block w-25">
                  <Input
                    onChange={(evt) => {
                      this.setState({
                        selectedOption: parseInt(evt.target.value),
                      })
                    }}
                    type="select"
                  >
                    {this.fixedOptions.map((option, index) => (
                      <option key={option} value={index}>
                        {t(option)}
                      </option>
                    ))}
                    {identifierTypes.map((identifierType, index) => (
                      <option
                        key={identifierType.uuid}
                        value={index + this.fixedOptions.length}
                      >
                        {identifierType.label}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
                <FormGroup className="d-inline-block pl-1 w-75">
                  {showAutocomplete ? (
                    <Autocomplete
                      defaultLanguage={defaultLanguage}
                      onSelect={this.addIdentifiableToList}
                      placeholder={t('autocomplete.searchTerm')}
                      search={(
                        contextPath,
                        mock,
                        searchTerm,
                        pageNumber,
                        pageSize
                      ) =>
                        searchIdentifiables(
                          contextPath,
                          mock,
                          searchTerm,
                          type,
                          pageNumber,
                          pageSize
                        )
                      }
                    />
                  ) : (
                    <IdentifierSearch
                      defaultLanguage={defaultLanguage}
                      namespace={
                        identifierTypes[
                          selectedOption - this.fixedOptions.length
                        ].namespace
                      }
                      onSelect={this.addIdentifiableToList}
                      type={type}
                    />
                  )}
                </FormGroup>
              </>
            )}
            {identifiables.length > 0 && (
              <ListGroup className="mb-3">
                {maxElements !== 1 && (
                  <Alert className="mb-0" color="info">
                    {t('duplicateInformation')}
                  </Alert>
                )}
                {identifiables.map(({label, previewImage}, index) => (
                  <ListGroupItem key={index}>
                    <Row>
                      <Col className="text-center" md="2">
                        <img
                          alt=""
                          className="img-fluid"
                          src={
                            previewImage
                              ? getImageUrl(
                                  previewImage,
                                  `${previewImageWidth},`
                                )
                              : `${this.context.apiContextPath}images/no-image.png`
                          }
                          style={{maxWidth: `${previewImageWidth}px`}}
                        />
                      </Col>
                      <Col md="9">
                        {label[defaultLanguage] ?? Object.values(label)[0]}
                      </Col>
                      <Col className="text-right" md="1">
                        <Button
                          className="p-0"
                          color="link"
                          onClick={() =>
                            this.setState({
                              identifiables: this.removeIdentifiableFromList(
                                index
                              ),
                            })
                          }
                        >
                          <FaTrash />
                        </Button>
                      </Col>
                    </Row>
                  </ListGroupItem>
                ))}
              </ListGroup>
            )}
            <Button
              className="float-right"
              color="primary"
              disabled={identifiables.length === 0}
              type="submit"
            >
              {t(action)}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

AddAttachedIdentifiablesModal.contextType = ApiContext

export default withTranslation()(AddAttachedIdentifiablesModal)
