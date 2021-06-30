import {Component} from 'react'
import {FaSearch} from 'react-icons/fa'
import {
  Button,
  Col,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  Row,
} from 'reactstrap'

import {findByIdentifier} from '../api'
import AppContext from './AppContext'
import InputWithSpinner from './InputWithSpinner'
import PreviewImage from './PreviewImage'
import {getLabelValue} from './utils'

class IdentifierSearch extends Component {
  constructor(props) {
    super(props)
    this.state = {
      id: '',
      loading: false,
      result: null,
    }
  }

  componentWillUnmount() {
    this.props.setFeedbackMessage(undefined)
  }

  search = async () => {
    this.setState({loading: true})
    const {namespace, setFeedbackMessage, type} = this.props
    const result = await findByIdentifier(
      this.context.apiContextPath,
      this.state.id,
      namespace,
      type
    )
    const isEmptyResult = Object.keys(result).length === 0
    if (isEmptyResult) {
      setFeedbackMessage({
        color: 'warning',
        key: `${type}NotFound`,
      })
    } else {
      setFeedbackMessage(undefined)
    }
    this.setState({isEmptyResult, loading: false, result})
  }

  render() {
    const {id, isEmptyResult, loading, result} = this.state
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
        {result && !isEmptyResult && (
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
                  {getLabelValue(
                    result.label,
                    this.props.activeLanguage,
                    this.context.defaultLanguage
                  )}
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
