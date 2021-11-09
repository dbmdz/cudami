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

const Input = ({inputProps, loading}) => (
  <FormGroup className="mb-0">
    <InputWithSpinner inputProps={inputProps} loading={loading} />
  </FormGroup>
)

const Suggestion = ({
  activeLanguage,
  defaultLanguage,
  suggestion: {label, previewImage, previewImageRenderingHints},
}) => (
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

const SuggestionsContainer = ({
  children,
  containerProps,
  loading,
  maxElements,
  minLength,
  searchTerm,
  totalElements,
}) => (
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
    {!loading && searchTerm.length >= minLength && totalElements === 0 && (
      <FeedbackMessage
        className="mb-0 text-center"
        message={{
          color: 'warning',
          key: 'noElementsFound',
        }}
      />
    )}
    {children}
  </div>
)

const search = async (apiContextPath, maxElements, onSearch, searchTerm) => {
  const {content: suggestions, totalElements} = await onSearch(
    apiContextPath,
    searchTerm,
    0,
    maxElements,
  )
  return {suggestions, totalElements}
}

const Autocomplete = ({
  activeLanguage,
  maxElements = 25,
  minLength = 2,
  onSearch,
  onSelect,
  placeholder,
}) => {
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
        setLoading(true)
        const {suggestions, totalElements} = await search(
          apiContextPath,
          maxElements,
          onSearch,
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
      renderInputComponent={(inputProps) => (
        <Input inputProps={inputProps} loading={loading} />
      )}
      renderSuggestion={(suggestion) => (
        <Suggestion
          activeLanguage={activeLanguage}
          defaultLanguage={defaultLanguage}
          suggestion={suggestion}
        />
      )}
      renderSuggestionsContainer={({children, containerProps}) => (
        <SuggestionsContainer
          containerProps={containerProps}
          loading={loading}
          maxElements={maxElements}
          minLength={minLength}
          searchTerm={searchTerm}
          totalElements={totalElements}
        >
          {children}
        </SuggestionsContainer>
      )}
      shouldRenderSuggestions={(value) => value.trim().length >= minLength}
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
