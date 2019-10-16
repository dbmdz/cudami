import React, {Component} from 'react'
import {Container, Label} from 'reactstrap'

import './IdentifiableForm.css'
import ArticleForm from './ArticleForm'
import ContentNodeForm from './ContentNodeForm'
import ContentTreeForm from './ContentTreeForm'
import CollectionForm from './CollectionForm'
import FileResourceForm from './FileResourceForm'
import FormErrors from './FormErrors'
import WebpageForm from './WebpageForm'
import WebsiteForm from './WebsiteForm'
import {
  getAvailableLanguages,
  loadAvailableLanguages,
  loadIdentifiable,
  saveIdentifiable,
  updateIdentifiable,
} from '../api'
import IFrameAdderModal from './modals/IFrameAdderModal'
import LanguageAdderModal from './modals/LanguageAdderModal'
import LinkAdderModal from './modals/LinkAdderModal'
import TableAdderModal from './modals/TableAdderModal'
import initI18n from '../i18n'

class IdentifiableForm extends Component {
  constructor(props) {
    super(props)
    this.identifiablesWithLongText = ['article', 'webpage']
    this.state = {
      activeLanguage: props.activeLanguage,
      availableLanguages: [],
      identifiable: null,
      invalidLanguages: [],
      modalsOpen: {
        iframeAdder: false,
        languageAdder: false,
        linkAdder: false,
        tableAdder: false,
      },
    }
  }

  async componentDidMount() {
    const i18n = initI18n(this.props.uiLocale)
    const availableLanguages = this.props.mockApi
      ? getAvailableLanguages()
      : await loadAvailableLanguages(this.props.apiContextPath)
    let identifiable = await loadIdentifiable(
      this.props.apiContextPath,
      this.props.type,
      this.props.uuid || 'new'
    )
    identifiable = {
      description: {},
      label: {
        [this.state.activeLanguage]: '',
      },
      text: this.identifiablesWithLongText.includes(this.props.type)
        ? {}
        : undefined,
      ...identifiable,
    }
    this.setState({
      availableLanguages: availableLanguages
        .reduce((languages, language) => {
          if (!(language in identifiable.label)) {
            languages.push({
              displayName: i18n.t(`languageNames:${language}`),
              name: language,
            })
          }
          return languages
        }, [])
        .sort((a, b) => (a.displayName > b.displayName ? 1 : -1)),
      identifiable,
    })
  }

  addLanguage = (selectedLanguage, modalName) => {
    this.setState({
      activeLanguage: selectedLanguage.name,
      availableLanguages: this.state.availableLanguages.filter(
        language => language.name !== selectedLanguage.name
      ),
      identifiable: {
        ...this.state.identifiable,
        label: {
          ...this.state.identifiable.label,
          [selectedLanguage.name]: '',
        },
      },
      modalsOpen: {
        ...this.state.modalsOpen,
        [modalName]: !this.state.modalsOpen[modalName],
      },
    })
  }

  /*
   * Removes languages with empty content from the json
   */
  cleanUpJson = editorJson => {
    const cleanedJson = Object.entries(editorJson).reduce(
      (json, [language, doc]) => {
        if (this.isEmptyContent(doc.content)) {
          return json
        }
        return {
          ...json,
          [language]: doc,
        }
      },
      {}
    )
    if (Object.keys(cleanedJson).length > 0) {
      return cleanedJson
    }
  }

  getFormComponent = () => {
    const FORM_COMPONENT_MAPPING = {
      article: ArticleForm,
      contentNode: ContentNodeForm,
      contentTree: ContentTreeForm,
      collection: CollectionForm,
      fileResource: FileResourceForm,
      webpage: WebpageForm,
      website: WebsiteForm,
    }
    const FormComponent = FORM_COMPONENT_MAPPING[this.props.type]
    return (
      <FormComponent
        activeLanguage={this.state.activeLanguage}
        apiContextPath={this.props.apiContextPath}
        canAddLanguage={this.state.availableLanguages.length > 0}
        identifiable={this.state.identifiable}
        onAddLanguage={this.toggleModal}
        onSubmit={this.submitIdentifiable}
        onToggleLanguage={this.toggleLanguage}
        onUpdate={this.updateIdentifiable}
        type={this.props.type}
      />
    )
  }

  isEmptyContent = content => {
    return content.length === 1 && !content[0].content
  }

  isFormValid = () => {
    let invalidLanguages = []
    const label = this.state.identifiable.label
    for (let language in label) {
      if (label[language] === '') {
        invalidLanguages.push(language)
      }
    }
    if (invalidLanguages.length > 0) {
      this.setState({
        invalidLanguages,
      })
      return false
    }
    return true
  }

  submitIdentifiable = () => {
    if (this.isFormValid()) {
      const identifiable = {
        ...this.state.identifiable,
        description: this.cleanUpJson(this.state.identifiable.description),
      }
      if (identifiable.text) {
        identifiable.text = this.cleanUpJson(this.state.identifiable.text)
      }
      if (identifiable.uuid) {
        updateIdentifiable(
          this.props.apiContextPath,
          identifiable,
          this.props.type
        )
      } else {
        saveIdentifiable(
          this.props.apiContextPath,
          identifiable,
          this.props.parentType,
          this.props.parentUuid,
          this.props.type
        )
      }
    }
  }

  toggleLanguage = activeLanguage => {
    this.setState({
      activeLanguage,
    })
  }

  toggleModal = name => {
    this.setState({
      modalsOpen: {
        ...this.state.modalsOpen,
        [name]: !this.state.modalsOpen[name],
      },
    })
  }

  updateIdentifiable = identifiable => {
    this.setState({
      identifiable,
    })
  }

  render() {
    return this.state.identifiable ? (
      <Container className="cudami-editor">
        {this.state.invalidLanguages.length > 0 && (
          <FormErrors invalidLanguages={this.state.invalidLanguages} />
        )}
        {this.getFormComponent()}
        {this.props.debug && (
          <>
            <Label className="font-weight-bold mt-3">JSON (debug)</Label>
            <pre className="border">
              <code>{JSON.stringify(this.state.identifiable, null, 4)}</code>
            </pre>
          </>
        )}
        <IFrameAdderModal
          isOpen={this.state.modalsOpen.iframeAdder}
          onToggle={() => this.toggleModal('iframeAdder')}
        />
        <LanguageAdderModal
          availableLanguages={this.state.availableLanguages}
          isOpen={this.state.modalsOpen.languageAdder}
          onClick={language => this.addLanguage(language, 'languageAdder')}
          onToggle={() => this.toggleModal('languageAdder')}
        />
        <LinkAdderModal
          isOpen={this.state.modalsOpen.linkAdder}
          onToggle={() => this.toggleModal('linkAdder')}
        />
        <TableAdderModal
          isOpen={this.state.modalsOpen.tableAdder}
          onToggle={() => this.toggleModal('tableAdder')}
        />
      </Container>
    ) : null
  }
}

IdentifiableForm.defaultProps = {
  apiContextPath: '/',
  debug: false,
  mockApi: false,
}

export default IdentifiableForm
