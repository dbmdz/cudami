import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Button,
  ButtonGroup,
  Form,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'

class AddLanguageDialog extends Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  addLanguage = () => {
    const {addLanguage, availableLanguages} = this.props
    const selectedLanguage = availableLanguages.filter(
      (language) => language.name === this.state.selectedLanguage,
    )[0]
    addLanguage(selectedLanguage)
    this.destroy()
  }

  destroy = () => {
    this.props.onToggle()
  }

  setLanguage = (selectedLanguage) => {
    this.setState({
      selectedLanguage,
    })
  }

  render() {
    const {availableLanguages, isOpen, t} = this.props
    return (
      <Modal
        isOpen={isOpen}
        onOpened={() => this.setLanguage(availableLanguages[0].name)}
        toggle={this.destroy}
      >
        <ModalHeader toggle={this.destroy}>{t('chooseLanguage')}</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={(evt) => {
              evt.preventDefault()
              this.addLanguage()
            }}
          >
            <FormGroup>
              <Input
                onChange={(evt) => this.setLanguage(evt.target.value)}
                type="select"
              >
                {availableLanguages.map((language) => (
                  <option key={language.name} value={language.name}>
                    {language.displayName}
                  </option>
                ))}
              </Input>
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

export default withTranslation()(AddLanguageDialog)
