import React, {Component} from 'react'
import {FormGroup, Input} from 'reactstrap'
import Autosuggest from 'react-autosuggest'

import {searchImages} from '../../../api'

class ImageAutocomplete extends Component {
  constructor(props) {
    super(props)
    this.state = {
      suggestions: [],
      value: '',
    }
  }

  getSuggestionAsString = (suggestion) =>
    `${suggestion.name} (${suggestion.year})`

  onChange = (_, {newValue}) => {
    this.setState({
      value: newValue,
    })
  }

  onSuggestionsClearRequested = () => {
    this.setState({
      suggestions: [],
    })
  }

  onSuggestionsFetchRequested = async ({value}) => {
    const suggestions = await searchImages(value)
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
    const {value, suggestions} = this.state
    const inputProps = {
      onChange: this.onChange,
      placeholder: 'Type to search in label, description and filename...',
      value,
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
