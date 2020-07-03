import React, {Component} from 'react'
import {Alert, FormGroup, Input} from 'reactstrap'
import Autosuggest from 'react-autosuggest'
import {withTranslation} from 'react-i18next'

import {getImageUrl} from '../../utils'
import {ApiContext, searchImages} from '../../../api'

class ImageAutocomplete extends Component {
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
    const {activeLanguage, defaultLanguage} = this.props
    if (label[activeLanguage]) {
      return label[activeLanguage]
    } else if (label[defaultLanguage]) {
      return label[defaultLanguage]
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
    const {suggestions, totalElements} = await searchImages(
      this.context.apiContextPath,
      searchTerm,
      0,
      this.maxElements
    )
    this.setState({
      suggestions,
      totalElements,
    })
  }

  renderInputComponent = (inputProps) => {
    return (
      <FormGroup className="mb-0">
        <Input {...inputProps} />
      </FormGroup>
    )
  }

  renderSuggestion = ({label, previewImage}) => {
    return (
      <>
        <div className="mr-3 suggestion-image">
          <img className="img-fluid" src={getImageUrl(previewImage, '50,')} />
        </div>
        {this.getLabelValue(label)}
      </>
    )
  }

  renderSuggestionsContainer = ({containerProps, children}) => {
    return (
      <div {...containerProps}>
        {this.state.totalElements > this.maxElements && (
          <Alert className="mb-0" color="info">
            {this.props.t('selectImage.moreElementsFound', {
              maxElements: this.maxElements,
              totalElements: this.state.totalElements,
            })}
          </Alert>
        )}
        {children}
      </div>
    )
  }

  selectFileResource = (_, {suggestion}) => {
    this.props.onChange({
      ...suggestion.previewImage,
      ...suggestion,
      uri: getImageUrl(suggestion.previewImage),
    })
  }

  render() {
    const {searchTerm, suggestions} = this.state
    const inputProps = {
      onChange: this.onChange,
      placeholder: this.props.t('selectImage.searchTerm'),
      value: searchTerm,
    }
    return (
      <Autosuggest
        getSuggestionValue={this.getSuggestionAsString}
        inputProps={inputProps}
        onSuggestionsClearRequested={this.onSuggestionsClearRequested}
        onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
        onSuggestionSelected={this.selectFileResource}
        renderInputComponent={this.renderInputComponent}
        renderSuggestion={this.renderSuggestion}
        renderSuggestionsContainer={this.renderSuggestionsContainer}
        suggestions={suggestions}
        theme={{
          suggestion: 'align-items-center d-flex list-group-item',
          suggestionHighlighted: 'active',
          suggestionsContainer: 'suggestion-container',
          suggestionsList: 'list-group suggestion-list',
        }}
      />
    )
  }
}

ImageAutocomplete.contextType = ApiContext

export default withTranslation()(ImageAutocomplete)
