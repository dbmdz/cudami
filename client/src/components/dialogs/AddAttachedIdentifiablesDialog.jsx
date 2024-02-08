import startCase from 'lodash-es/startCase'
import {useContext, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  ButtonGroup,
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

import {search} from '../../api'
import AppContext from '../AppContext'
import Autocomplete from '../Autocomplete'
import FeedbackMessage from '../FeedbackMessage'
import IdentifierSearch from '../IdentifierSearch'
import PreviewImage from '../PreviewImage'
import {getLabelValue} from '../utils'

const AddAttachedIdentifiablesDialog = ({
  action,
  activeLanguage,
  identifierTypes,
  isOpen,
  maxElements,
  onSubmit,
  toggle,
  type,
}) => {
  const {defaultLanguage} = useContext(AppContext)
  const [identifiables, setIdentifiables] = useState([])
  const [selectedOption, setSelectedOption] = useState(0)
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setIdentifiables([])
    setSelectedOption(0)
  }
  const fixedIdentifiers = [
    {
      label: 'UUID',
      namespace: 'uuid',
    },
    {
      namespace: 'refId',
    },
  ]
  const fixedOptions = ['label']
  const showAutocomplete = selectedOption < fixedOptions.length
  const showInputFields =
    maxElements === undefined || identifiables.length < maxElements
  return (
    <Modal isOpen={isOpen} size="lg" toggle={destroy}>
      <ModalHeader toggle={destroy}>
        {t(`${action}${startCase(type).replace(' ', '')}s`)}
      </ModalHeader>
      <ModalBody>
        <Form
          onSubmit={(evt) => {
            evt.preventDefault()
            onSubmit(identifiables)
            destroy()
          }}
        >
          {showInputFields && (
            <div className="d-flex">
              <FormGroup className="w-25">
                <Input
                  onChange={(evt) => {
                    setSelectedOption(parseInt(evt.target.value))
                  }}
                  type="select"
                >
                  {fixedOptions.map((option, index) => (
                    <option key={option} value={index}>
                      {t(option)}
                    </option>
                  ))}
                  {identifierTypes.map((identifierType, index) => (
                    <option
                      key={identifierType.uuid}
                      value={index + fixedOptions.length}
                    >
                      {identifierType.label}
                    </option>
                  ))}
                  {fixedIdentifiers.map(({label, namespace}, index) => (
                    <option
                      key={namespace}
                      value={
                        index + fixedOptions.length + identifierTypes.length
                      }
                    >
                      {label ?? t(namespace)}
                    </option>
                  ))}
                </Input>
              </FormGroup>
              <FormGroup className="d-inline-block pl-1 w-75">
                {showAutocomplete ? (
                  <Autocomplete
                    activeLanguage={activeLanguage}
                    onSearch={(contextPath, searchTerm, pageNumber, pageSize) =>
                      search(contextPath, type, {
                        pageNumber,
                        pageSize,
                        searchTerm,
                        sorting: {
                          orders: [
                            {property: 'label', subProperty: defaultLanguage},
                          ],
                        },
                      })
                    }
                    onSelect={(identifiable) =>
                      setIdentifiables([...identifiables, identifiable])
                    }
                  />
                ) : (
                  <IdentifierSearch
                    activeLanguage={activeLanguage}
                    fixedIdentifiers={fixedIdentifiers}
                    namespace={
                      identifierTypes[selectedOption - fixedOptions.length]
                        ?.namespace ??
                      fixedIdentifiers[
                        selectedOption -
                          fixedOptions.length -
                          identifierTypes.length
                      ].namespace
                    }
                    onSelect={(identifiable) =>
                      setIdentifiables([...identifiables, identifiable])
                    }
                    type={type}
                  />
                )}
              </FormGroup>
            </div>
          )}
          {identifiables.length > 0 && (
            <ListGroup className="mb-3">
              {maxElements !== 1 && (
                <FeedbackMessage
                  message={{key: 'duplicateInformation.attachedIdentifiables'}}
                />
              )}
              {identifiables.map(
                ({label, previewImage, previewImageRenderingHints, uuid}) => (
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
                        {getLabelValue(label, activeLanguage, defaultLanguage)}
                      </Col>
                      <Col className="text-right" md="1">
                        <Button
                          className="p-0"
                          color="link"
                          onClick={() =>
                            setIdentifiables(
                              identifiables.filter(({uuid: u}) => u !== uuid),
                            )
                          }
                        >
                          <FaTrashAlt />
                        </Button>
                      </Col>
                    </Row>
                  </ListGroupItem>
                ),
              )}
            </ListGroup>
          )}
          <ButtonGroup className="float-right">
            <Button className="mr-1" color="light" onClick={destroy}>
              {t('cancel')}
            </Button>
            <Button
              color="primary"
              disabled={identifiables.length === 0}
              type="submit"
            >
              {t(action)}
            </Button>
          </ButtonGroup>
        </Form>
      </ModalBody>
    </Modal>
  )
}

export default AddAttachedIdentifiablesDialog
