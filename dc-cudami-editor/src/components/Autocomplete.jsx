import './Autocomplete.css'

import {useContext, useState} from 'react'
import Autosuggest from 'react-autosuggest'
import {useTranslation} from 'react-i18next'
import {Col, FormGroup, Row} from 'reactstrap'

import AppContext from './AppContext'
import FeedbackMessage from './FeedbackMessage'
import InputWithSpinner from './InputWithSpinner'
import PreviewImage from './PreviewImage'
import {getLabelValue} from './utils'

const search = async ({maxElements, onSearch}, apiContextPath, searchTerm) => {
  const {content: suggestions, totalElements} = await onSearch(
    apiContextPath,
    searchTerm,
    0,
    maxElements,
  )
  return {suggestions, totalElements}
}

const renderInputComponent = (inputProps, loading) => (
  <FormGroup className="mb-0">
    <InputWithSpinner inputProps={inputProps} loading={loading} />
  </FormGroup>
)

const renderSuggestion = (
  activeLanguage,
  defaultLanguage,
  {label, previewImage, previewImageRenderingHints},
) => (
  <Row>
    <Col md="1">
      <PreviewImage
        image={previewImage}
        renderingHints={previewImageRenderingHints}
        width={50}
      />
    </Col>
    <Col className="text-left" md="11">
      {getLabelValue(label, activeLanguage, defaultLanguage)}
    </Col>
  </Row>
)

const renderSuggestionsContainer = (
  children,
  containerProps,
  maxElements,
  totalElements,
) => (
  <div {...containerProps}>
    {totalElements > maxElements && (
      <FeedbackMessage
        className="mb-0 text-center"
        message={{
          key: 'moreElementsFound',
          values: {
            maxElements,
            totalElements,
          },
        }}
      />
    )}
    {children}
  </div>
)

const Autocomplete = (props) => {
  const {activeLanguage, maxElements = 25, onSelect, placeholder} = props
  const {apiContextPath, defaultLanguage} = useContext(AppContext)
  const [loading, setLoading] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [totalElements, setTotalElements] = useState(0)
  const {t} = useTranslation()
  const inputProps = {
    onChange: (evt) => setSearchTerm(evt.target.value),
    placeholder: placeholder ?? t('searchTerm'),
    value: searchTerm,
  }
  return (
    <Autosuggest
      getSuggestionValue={({label}) =>
        `${getLabelValue(label, activeLanguage, defaultLanguage)}`
      }
      inputProps={inputProps}
      onSuggestionsClearRequested={() => {
        setSuggestions([])
        setTotalElements(0)
      }}
      onSuggestionsFetchRequested={async ({value: searchTerm}) => {
        if (searchTerm.length < 2) {
          return
        }
        setLoading(true)
        const {suggestions, totalElements} = await search(
          props,
          apiContextPath,
          searchTerm,
        )
        setLoading(false)
        setSuggestions(suggestions)
        setTotalElements(totalElements)
      }}
      onSuggestionSelected={(_evt, {suggestion}) => {
        setSearchTerm('')
        onSelect(suggestion)
      }}
      renderInputComponent={(inputProps) =>
        renderInputComponent(inputProps, loading)
      }
      renderSuggestion={(suggestion) =>
        renderSuggestion(activeLanguage, defaultLanguage, suggestion)
      }
      renderSuggestionsContainer={({children, containerProps}) =>
        renderSuggestionsContainer(
          children,
          containerProps,
          maxElements,
          totalElements,
        )
      }
      suggestions={suggestions}
      theme={{
        suggestion: 'list-group-item',
        suggestionHighlighted: 'active',
        suggestionsContainer: 'suggestion-container',
        suggestionsList: 'list-group',
      }}
    />
  )
}

export default Autocomplete
