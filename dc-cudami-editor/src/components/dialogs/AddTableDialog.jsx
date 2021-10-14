import {publish, subscribe} from 'pubsub-js'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Input,
  Label,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

class AddTableDialog extends Component {
  constructor(props) {
    super(props)
    this.state = {
      columns: 2,
      rows: 2,
    }
    subscribe('editor.show-table-dialog', () => {
      this.props.onToggle()
    })
  }

  addTableToEditor = () => {
    publish('editor.add-table', this.state)
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
    this.setState({
      columns: 2,
      rows: 2,
    })
  }

  render() {
    const {isOpen, t} = this.props
    return (
      <Modal isOpen={isOpen} toggle={this.destroy}>
        <ModalHeader toggle={this.destroy}>{t('insert.table')}</ModalHeader>
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
            <ButtonGroup className="float-right">
              <Button className="mr-1" color="light" onClick={this.destroy}>
                {t('cancel')}
              </Button>
              <Button color="primary" type="submit">
                {t('add')}
              </Button>
            </ButtonGroup>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(AddTableDialog)
