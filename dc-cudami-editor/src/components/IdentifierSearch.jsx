import React, {Component} from 'react'
import {
  Button,
  Col,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  Row,
} from 'reactstrap'
import {FaSearch} from 'react-icons/fa'

import AppContext from './AppContext'
import InputWithSpinner from './InputWithSpinner'
import PreviewImage from './PreviewImage'
import {findByIdentifier} from '../api'

class IdentifierSearch extends Component {
  constructor(props) {
    super(props)
    this.state = {
      id: '',
      loading: false,
      result: null,
    }
  }

  search = async () => {
    this.setState({loading: true})
    const result = await findByIdentifier(
      this.context.apiContextPath,
      this.context.mockApi,
      this.state.id,
      this.props.namespace,
      this.props.type
    )
    this.setState({loading: false, result})
  }

  render() {
    const {id, loading, result} = this.state
    return (
      <>
        <InputWithSpinner
          inputProps={{
            onChange: (evt) => this.setState({id: evt.target.value}),
            value: id,
          }}
          loading={loading}
        >
          <InputGroupAddon addonType="append">
            <Button color="primary" disabled={!id.length} onClick={this.search}>
              <FaSearch />
            </Button>
          </InputGroupAddon>
        </InputWithSpinner>
        {result && (
          <ListGroup className="suggestion-container">
            <ListGroupItem
              onClick={() => {
                this.props.onSelect(result)
                this.setState({id: '', result: null})
              }}
            >
              <Row>
                <Col md="1">
                  <PreviewImage
                    image={result.previewImage}
                    renderingHints={result.previewImageRenderingHints}
                    width={50}
                  />
                </Col>
                <Col md="11">
                  {result.label[this.context.defaultLanguage] ??
                    Object.values(result.label)[0]}
                </Col>
              </Row>
            </ListGroupItem>
          </ListGroup>
        )}
      </>
    )
  }
}

IdentifierSearch.contextType = AppContext

export default IdentifierSearch
