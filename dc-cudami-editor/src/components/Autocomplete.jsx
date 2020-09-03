import React, {Component} from 'react'
import {Alert, Col, FormGroup, Input, Row} from 'reactstrap'
import Autosuggest from 'react-autosuggest'
import {withTranslation} from 'react-i18next'

import './Autocomplete.css'
import AppContext from './AppContext'
import PreviewImage from './PreviewImage'

class Autocomplete extends Component {
  /* defines the number of suggestions to be fetched */
  maxElements = 25

  constructor(props) {
    super(props)
    this.state = {
      searchTerm: '',
      suggestions: [],
      totalElements: 0,
    }
  }

  getLabelValue = (label) => {
    if (label[this.props.activeLanguage]) {
      return label[this.props.activeLanguage]
    } else if (label[this.context.defaultLanguage]) {
      return label[this.context.defaultLanguage]
    }
    return Object.values(label)[0]
  }

  getSuggestionAsString = (suggestion) =>
    `${this.getLabelValue(suggestion.label)}`

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
    const {suggestions, totalElements} = await this.props.search(
      this.context.apiContextPath,
      this.context.mockApi,
      searchTerm,
      0,
      this.maxElements
    )
    this.setState({
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
        <Input {...inputProps} />
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
          {this.getLabelValue(label)}
        </Col>
      </Row>
    )
  }

  renderSuggestionsContainer = ({containerProps, children}) => {
    return (
      <div {...containerProps}>
        {this.state.totalElements > this.maxElements && (
          <Alert className="mb-0" color="info">
            {this.props.t('autocomplete.moreElementsFound', {
              maxElements: this.maxElements,
              totalElements: this.state.totalElements,
            })}
          </Alert>
        )}
        {children}
      </div>
    )
  }

  render() {
    const {searchTerm, suggestions} = this.state
    const inputProps = {
      onChange: this.onChange,
      placeholder: this.props.placeholder,
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
