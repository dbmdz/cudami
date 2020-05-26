import React, {Component} from 'react'
import {
  Button,
  Form,
  FormGroup,
  Input,
  Modal,
  ModalBody,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class LanguageAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  addLanguage = () => {
    const {availableLanguages, onClick} = this.props
    const selectedLanguage = availableLanguages.filter(
      (language) => language.name === this.state.selectedLanguage
    )[0]
    onClick(selectedLanguage)
  }

  setLanguage = (selectedLanguage) => {
    this.setState({
      selectedLanguage,
    })
  }

  render() {
    const {availableLanguages, isOpen, onToggle, t} = this.props
    return (
      <Modal
        isOpen={isOpen}
        onOpened={() => this.setLanguage(availableLanguages[0].name)}
        toggle={onToggle}
      >
        <ModalHeader toggle={onToggle}>{t('chooseLanguage')}</ModalHeader>
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
            <Button className="float-right" color="primary" type="submit">
              {t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(LanguageAdderModal)
