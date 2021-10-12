import './Autocomplete.css'

import {Component} from 'react'
import Autosuggest from 'react-autosuggest'
import {withTranslation} from 'react-i18next'
import {Col, FormGroup, Row} from 'reactstrap'

import AppContext from './AppContext'
import FeedbackMessage from './FeedbackMessage'
import InputWithSpinner from './InputWithSpinner'
import PreviewImage from './PreviewImage'
import {getLabelValue} from './utils'

class Autocomplete extends Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
      /* defines the number of suggestions to be fetched */
      maxElements: props.maxElements ?? 25,
      searchTerm: '',
      suggestions: [],
      totalElements: 0,
    }
  }

  getSuggestionAsString = ({label}) =>
    `${getLabelValue(
      label,
      this.props.activeLanguage,
      this.context.defaultLanguage,
    )}`

  onChange = (_, {newValue: searchTerm}) => {
    this.setState({
      searchTerm,
    })
  }

  onSuggestionsClearRequested = () => {
    this.setState({
      suggestions: [],
      totalElements: 0,
    })
  }

  onSuggestionsFetchRequested = async ({value: searchTerm}) => {
    if (searchTerm.length < 2) {
      return
    }
    this.setState({loading: true})
    const {content: suggestions, totalElements} = await this.props.search(
      this.context.apiContextPath,
      searchTerm,
      0,
      this.state.maxElements,
    )
    this.setState({
      loading: false,
      suggestions,
      totalElements,
    })
  }

  onSuggestionSelected = (_, {suggestion}) => {
    this.setState({
      searchTerm: '',
    })
    this.props.onSelect(suggestion)
  }

  renderInputComponent = (inputProps) => {
    return (
      <FormGroup className="mb-0">
        <InputWithSpinner
          inputProps={inputProps}
          loading={this.state.loading}
        />
      </FormGroup>
    )
  }

  renderSuggestion = ({label, previewImage, previewImageRenderingHints}) => {
    return (
      <Row>
        <Col md="1">
          <PreviewImage
            image={previewImage}
            renderingHints={previewImageRenderingHints}
            width={50}
          />
        </Col>
        <Col className="text-left" md="11">
          {getLabelValue(
            label,
            this.props.activeLanguage,
            this.context.defaultLanguage,
          )}
        </Col>
      </Row>
    )
  }

  renderSuggestionsContainer = ({containerProps, children}) => {
    const {maxElements, totalElements} = this.state
    return (
      <div {...containerProps}>
        {totalElements > maxElements && (
          <FeedbackMessage
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
  }

  render() {
    const {searchTerm, suggestions} = this.state
    const {placeholder, t} = this.props
    const inputProps = {
      onChange: this.onChange,
      placeholder: placeholder ?? t('searchTerm'),
      value: searchTerm,
    }
    return (
      <Autosuggest
        getSuggestionValue={this.getSuggestionAsString}
        inputProps={inputProps}
        onSuggestionsClearRequested={this.onSuggestionsClearRequested}
        onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
        onSuggestionSelected={this.onSuggestionSelected}
        renderInputComponent={this.renderInputComponent}
        renderSuggestion={this.renderSuggestion}
        renderSuggestionsContainer={this.renderSuggestionsContainer}
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
}

Autocomplete.contextType = AppContext

export default withTranslation()(Autocomplete)
