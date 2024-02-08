import isEmpty from 'lodash-es/isEmpty'
import {useContext, useEffect, useState} from 'react'
import {FaSearch} from 'react-icons/fa'
import {
  Button,
  Col,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  Row,
} from 'reactstrap'

import {getByIdentifier, getByUuid} from '../api'
import AppContext from './AppContext'
import FeedbackMessage from './FeedbackMessage'
import InputWithSpinner from './InputWithSpinner'
import PreviewImage from './PreviewImage'
import {getLabelValue} from './utils'

const search = async (
  {fixedIdentifiers, namespace, type},
  apiContextPath,
  id,
) => {
  const isFixedIdentifier = fixedIdentifiers.some(
    (i) => i.namespace === namespace,
  )
  const suggestion = await (isFixedIdentifier
    ? getByUuid(apiContextPath, type, id)
    : getByIdentifier(apiContextPath, id, namespace, type))
  return suggestion
}

export const IdentifierSearch = (props) => {
  const {activeLanguage, namespace, onSelect, type} = props
  const {apiContextPath, defaultLanguage} = useContext(AppContext)
  const [id, setId] = useState('')
  const [loading, setLoading] = useState(false)
  const [suggestion, setSuggestion] = useState()
  /*
   * Resets the input value and the loading state
   * and also the search result (see the other effect)
   * when the namespace received from props changes
   */
  useEffect(() => {
    setId('')
    setLoading(false)
  }, [namespace])
  /* Resets the search result, when the input value changes */
  useEffect(() => {
    setSuggestion(undefined)
  }, [id])
  return (
    <>
      <InputWithSpinner
        inputProps={{
          onChange: (evt) => setId(evt.target.value),
          value: id,
        }}
        loading={loading}
      >
        <InputGroupAddon addonType="append">
          <Button
            className="align-items-center d-flex"
            color="primary"
            disabled={!id.length}
            onClick={async () => {
              setLoading(true)
              const suggestion = await search(props, apiContextPath, id)
              setLoading(false)
              setSuggestion(suggestion)
            }}
          >
            <FaSearch />
          </Button>
        </InputGroupAddon>
      </InputWithSpinner>
      <div className="suggestion-container">
        {suggestion && isEmpty(suggestion) && (
          <FeedbackMessage
            className="mb-0 text-center"
            message={{
              color: 'warning',
              key: `${type}NotFound`,
            }}
          />
        )}
        {!isEmpty(suggestion) && (
          <ListGroup>
            <ListGroupItem
              onClick={() => {
                onSelect(suggestion)
                setId('')
                setSuggestion(undefined)
              }}
            >
              <Row>
                <Col md="1">
                  <PreviewImage
                    image={suggestion.previewImage}
                    renderingHints={suggestion.previewImageRenderingHints}
                    width={50}
                  />
                </Col>
                <Col md="11">
                  {getLabelValue(
                    suggestion.label,
                    activeLanguage,
                    defaultLanguage,
                  )}
                </Col>
              </Row>
            </ListGroupItem>
          </ListGroup>
        )}
      </div>
    </>
  )
}

export default IdentifierSearch
