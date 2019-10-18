import React, {Component} from 'react'
import {
  Button,
  Input,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'

class LanguageAdderModal extends Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  add = () => {
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
        </ModalBody>
        <ModalFooter>
          <Button color="primary" onClick={this.add}>
            {t('add')}
          </Button>
        </ModalFooter>
      </Modal>
    )
  }
}

export default withTranslation()(LanguageAdderModal)
