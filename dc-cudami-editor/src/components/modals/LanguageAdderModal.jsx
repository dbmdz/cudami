import React, {Component} from 'react'
import {Button, Form, Input, Modal, ModalBody, ModalHeader} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class LanguageAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  addLanguage = () => {
    const selectedLanguage = this.props.availableLanguages.filter(
      language => language.name === this.state.selectedLanguage
    )[0]
    this.props.onClick(selectedLanguage)
  }

  setLanguage = selectedLanguage => {
    this.setState({
      selectedLanguage,
    })
  }

  toggle = () => {
    this.props.onToggle()
  }

  render() {
    const {t} = this.props
    return (
      <Modal
        isOpen={this.props.isOpen}
        onOpened={() => this.setLanguage(this.props.availableLanguages[0].name)}
        toggle={this.toggle}
      >
        <ModalHeader toggle={this.toggle}>{t('chooseLanguage')}</ModalHeader>
        <ModalBody>
          <Form
            onSubmit={evt => {
              evt.preventDefault()
              this.addLanguage()
            }}
          >
            <Input
              onChange={evt => this.setLanguage(evt.target.value)}
              type="select"
            >
              {this.props.availableLanguages.map(language => (
                <option key={language.name} value={language.name}>
                  {language.displayName}
                </option>
              ))}
            </Input>
            <Button className="float-right mt-3" color="primary" type="submit">
              {t('add')}
            </Button>
          </Form>
        </ModalBody>
      </Modal>
    )
  }
}

export default withTranslation()(LanguageAdderModal)
