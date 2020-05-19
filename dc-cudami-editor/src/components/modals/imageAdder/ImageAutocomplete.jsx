import React, {Component} from 'react'
import {FormGroup, Input} from 'reactstrap'
import Autosuggest from 'react-autosuggest'

import {searchImages} from '../../../api'

class ImageAutocomplete extends Component {
  constructor(props) {
    super(props)
    this.state = {
      searchTerm: '',
      suggestions: [],
    }
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
        renderSuggestion={this.getSuggestionAsString}
        suggestions={suggestions}
        theme={{
          suggestion: 'list-group-item text-left',
          suggestionHighlighted: 'active',
          suggestionsList: 'list-group',
        }}
      />
    )
  }
}

export default ImageAutocomplete
