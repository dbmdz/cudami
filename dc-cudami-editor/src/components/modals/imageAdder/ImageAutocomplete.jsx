import React, {Component} from 'react'
import {FormGroup, Input} from 'reactstrap'
import Autosuggest from 'react-autosuggest'

import './ImageAutocomplete.css'
import {searchImages} from '../../../api'

class ImageAutocomplete extends Component {
  constructor(props) {
    super(props)
    this.state = {
      searchTerm: '',
      suggestions: [],
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
    `${suggestion.name} (${suggestion.year})`

  onChange = (_, {newValue: searchTerm}) => {
    this.setState({
      searchTerm,
    })
  }

  onSuggestionsClearRequested = () => {
    this.setState({
      suggestions: [],
    })
  }

  onSuggestionsFetchRequested = async ({value: searchTerm}) => {
    if (searchTerm.length < 2) {
      return
    }
    const suggestions = await searchImages(
      this.props.apiContextPath,
      searchTerm
    )
    this.setState({
      suggestions,
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
    const previewImageUrl = previewImage.iiifBaseUrl
      ? `${previewImage.iiifBaseUrl}/full/50,/0/default.${previewImage.filenameExtension}`
      : previewImage.uri
    return (
      <>
        <div className="mr-3 suggestion-image">
          <img className="img-fluid" src={previewImageUrl} />
        </div>
        {this.getLabelValue(label)}
      </>
    )
  }

  render() {
    const {searchTerm, suggestions} = this.state
    const inputProps = {
      onChange: this.onChange,
      placeholder: 'Type to search in label, description and filename...',
      value: searchTerm,
    }
    return (
      <Autosuggest
        getSuggestionValue={this.getSuggestionAsString}
        inputProps={inputProps}
        onSuggestionsClearRequested={this.onSuggestionsClearRequested}
        onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
        renderInputComponent={this.renderInputComponent}
        renderSuggestion={this.renderSuggestion}
        suggestions={suggestions}
        theme={{
          suggestion: 'align-items-center d-flex list-group-item',
          suggestionHighlighted: 'active',
          suggestionsList: 'list-group suggestion-list',
        }}
      />
    )
  }
}

export default ImageAutocomplete
