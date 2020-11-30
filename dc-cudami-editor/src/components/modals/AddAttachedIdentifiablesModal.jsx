import startCase from 'lodash/startCase'
import React, {Component} from 'react'
import {
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

import AppContext from '../AppContext'
import Autocomplete from '../Autocomplete'
import FeedbackMessage from '../FeedbackMessage'
import IdentifierSearch from '../IdentifierSearch'
import PreviewImage from '../PreviewImage'
import {searchIdentifiables} from '../../api'

class AddAttachedIdentifiablesModal extends Component {
  fixedOptions = ['label']

  constructor(props) {
    super(props)
    this.state = {
      identifiables: [],
      selectedOption: 0,
    }
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
      identifierTypes,
      isOpen,
      maxElements,
      onSubmit,
      t,
      type,
    } = this.props
    const {feedbackMessage, identifiables, selectedOption} = this.state
    const showAutocomplete = selectedOption < this.fixedOptions.length
    const showInputFields =
      maxElements === undefined || identifiables.length < maxElements
    return (
      <Modal isOpen={isOpen} size="lg" toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>
          {t(`${action}${startCase(type).replace(' ', '')}s`)}
        </ModalHeader>
        <ModalBody>
          {feedbackMessage && (
            <FeedbackMessage
              className="mb-1"
              message={feedbackMessage}
              onClose={() => this.setState({feedbackMessage: undefined})}
            />
          )}
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
                      onSelect={this.addIdentifiableToList}
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
                      namespace={
                        identifierTypes[
                          selectedOption - this.fixedOptions.length
                        ].namespace
                      }
                      onSelect={this.addIdentifiableToList}
                      setFeedbackMessage={(feedbackMessage) =>
                        this.setState({feedbackMessage})
                      }
                      type={type}
                    />
                  )}
                </FormGroup>
              </>
            )}
            {identifiables.length > 0 && (
              <ListGroup className="mb-3">
                {maxElements !== 1 && (
                  <FeedbackMessage message={{key: 'duplicateInformation'}} />
                )}
                {identifiables.map(
                  (
                    {label, previewImage, previewImageRenderingHints, uuid},
                    index
                  ) => (
                    <ListGroupItem key={uuid}>
                      <Row>
                        <Col className="text-center" md="2">
                          <PreviewImage
                            image={previewImage}
                            renderingHints={previewImageRenderingHints}
                            width={50}
                          />
                        </Col>
                        <Col md="9">
                          {label[this.context.defaultLanguage] ??
                            Object.values(label)[0]}
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
                  )
                )}
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

AddAttachedIdentifiablesModal.contextType = AppContext

export default withTranslation()(AddAttachedIdentifiablesModal)
