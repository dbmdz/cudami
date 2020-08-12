import React, {Component} from 'react'
import {
  Button,
  Col,
  Input,
  InputGroup,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  Row,
} from 'reactstrap'
import {FaSearch} from 'react-icons/fa'

import {getImageUrl} from './utils'
import {ApiContext, findByIdentifier} from '../api'

class IdentifierSearch extends Component {
  constructor(props) {
    super(props)
    this.state = {
      id: '',
      result: null,
    }
  }

  search = async () => {
    const result = await findByIdentifier(
      this.context.apiContextPath,
      this.context.mockApi,
      this.state.id,
      this.props.namespace,
      this.props.type
    )
    this.setState({result})
  }

  render() {
    const {id, result} = this.state
    return (
      <>
        <InputGroup>
          <Input
            onChange={(evt) => this.setState({id: evt.target.value})}
            value={id}
          />
          <InputGroupAddon addonType="append">
            <Button color="primary" disabled={!id.length} onClick={this.search}>
              <FaSearch />
            </Button>
          </InputGroupAddon>
        </InputGroup>
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
                  <img
                    className="img-fluid"
                    src={getImageUrl(result.previewImage, '50,')}
                  />
                </Col>
                <Col md="11">
                  {result.label[this.props.defaultLanguage] ??
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

IdentifierSearch.contextType = ApiContext

export default IdentifierSearch
