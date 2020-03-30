import {publish, subscribe} from 'pubsub-js'
import React, {Component} from 'react'
import {
  Button,
  Form,
  FormGroup,
  Input,
  Label,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class TableAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {
      columns: 2,
      rows: 2,
    }
    subscribe('editor.show-table-modal', () => {
      this.props.onToggle()
    })
  }

  addTableToEditor = () => {
    this.props.onToggle()
    publish('editor.add-table', this.state)
    this.setState({
      columns: 2,
      rows: 2,
    })
  }

  render() {
    const {t} = this.props
    return (
      <Modal isOpen={this.props.isOpen} toggle={this.props.onToggle}>
        <ModalHeader toggle={this.props.onToggle}>
          {t('insert.table')}
        </ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              this.addTableToEditor()
            }}
          >
            <FormGroup>
              <Label className="font-weight-bold" for="table-rows">
                {t('numberOfRows')}
              </Label>
              <Input
                id="table-rows"
                min="1"
                onChange={(evt) =>
                  this.setState({rows: parseInt(evt.target.value)})
                }
                required
                type="number"
                value={this.state.rows}
              />
            </FormGroup>
            <FormGroup>
              <Label className="font-weight-bold" for="table-columns">
                {t('numberOfColumns')}
              </Label>
              <Input
                id="table-columns"
                min="1"
                onChange={(evt) =>
                  this.setState({columns: parseInt(evt.target.value)})
                }
                required
                type="number"
                value={this.state.columns}
              />
            </FormGroup>
            <Button className="float-right" color="primary" type="submit">
              {t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(TableAdderModal)
