import React, {Component} from 'react'
import {Container, Label} from 'reactstrap'

import './IdentifiableForm.css'
import ArticleForm from './ArticleForm'
import CollectionForm from './CollectionForm'
import CorporationForm from './CorporationForm'
import FileResourceForm from './FileResourceForm'
import FormErrors from './FormErrors'
import ProjectForm from './ProjectForm'
import SubtopicForm from './SubtopicForm'
import TopicForm from './TopicForm'
import WebpageForm from './WebpageForm'
import WebsiteForm from './WebsiteForm'
import {
  getAvailableLanguages,
  getDefaultLanguage,
  loadAvailableLanguages,
  loadDefaultLanguage,
  loadIdentifiable,
  saveIdentifiable,
  updateIdentifiable,
} from '../api'
import IframeAdderModal from './modals/IframeAdderModal'
import ImageAdderModal from './modals/ImageAdderModal'
import LanguageAdderModal from './modals/LanguageAdderModal'
import LinkAdderModal from './modals/LinkAdderModal'
import TableAdderModal from './modals/TableAdderModal'
import initI18n from '../i18n'
import '../polyfills'

class IdentifiableForm extends Component {
  identifiablesWithLongText = [
    'article',
    'collection',
    'corporation',
    'project',
    'webpage',
  ]

  constructor(props) {
    super(props)
    this.state = {
      activeLanguage: props.activeLanguage,
      availableLanguages: [],
      existingLanguages: props.existingLanguages || [props.activeLanguage],
      identifiable: null,
      invalidLanguages: [],
      modalsOpen: {
        iframeAdder: false,
        imageAdder: false,
        languageAdder: false,
        linkAdder: false,
        tableAdder: false,
      },
    }
  }

  async componentDidMount() {
    const {apiContextPath, mockApi, type, uiLocale, uuid} = this.props
    const i18n = initI18n(uiLocale)
    const availableLanguages = mockApi
      ? getAvailableLanguages()
      : await loadAvailableLanguages(apiContextPath)
    const defaultLanguage = mockApi
      ? getDefaultLanguage()
      : await loadDefaultLanguage(apiContextPath)
    let identifiable = await loadIdentifiable(
      apiContextPath,
      type,
      uuid || 'new'
    )
    identifiable = {
      description: {},
      label: {
        [this.state.activeLanguage]: '',
      },
      text: this.identifiablesWithLongText.includes(type) ? {} : undefined,
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
      defaultLanguage,
      identifiable,
    })
  }

  addLanguage = (selectedLanguage, modalName) => {
    this.setState({
      activeLanguage: selectedLanguage.name,
      availableLanguages: this.state.availableLanguages.filter(
        (language) => language.name !== selectedLanguage.name
      ),
      existingLanguages: [
        ...this.state.existingLanguages,
        selectedLanguage.name,
      ],
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
  cleanUpJson = (editorJson) => {
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
      collection: CollectionForm,
      corporation: CorporationForm,
      fileResource: FileResourceForm,
      project: ProjectForm,
      subtopic: SubtopicForm,
      topic: TopicForm,
      webpage: WebpageForm,
      website: WebsiteForm,
    }
    const FormComponent = FORM_COMPONENT_MAPPING[this.props.type]
    return (
      <FormComponent
        activeLanguage={this.state.activeLanguage}
        apiContextPath={this.props.apiContextPath}
        canAddLanguage={this.state.availableLanguages.length > 0}
        existingLanguages={this.state.existingLanguages}
        identifiable={this.state.identifiable}
        onAddLanguage={this.toggleModal}
        onSubmit={this.submitIdentifiable}
        onToggleLanguage={this.toggleLanguage}
        onUpdate={this.updateIdentifiable}
      />
    )
  }

  isEmptyContent = (content) => {
    return (
      content.length === 1 &&
      content[0].type === 'paragraph' &&
      !content[0].content
    )
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
      const {apiContextPath, parentType, parentUuid, type} = this.props
      const identifiable = {
        ...this.state.identifiable,
        description: this.cleanUpJson(this.state.identifiable.description),
      }
      if (identifiable.text) {
        identifiable.text = this.cleanUpJson(this.state.identifiable.text)
      }
      if (identifiable.uuid) {
        updateIdentifiable(apiContextPath, identifiable, type)
      } else {
        saveIdentifiable(
          apiContextPath,
          identifiable,
          parentType,
          parentUuid,
          type
        )
      }
    }
  }

  toggleLanguage = (activeLanguage) => {
    this.setState({
      activeLanguage,
    })
  }

  toggleModal = (name) => {
    this.setState({
      modalsOpen: {
        ...this.state.modalsOpen,
        [name]: !this.state.modalsOpen[name],
      },
    })
  }

  updateIdentifiable = (identifiable) => {
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
        <IframeAdderModal
          isOpen={this.state.modalsOpen.iframeAdder}
          onToggle={() => this.toggleModal('iframeAdder')}
        />
        <ImageAdderModal
          activeLanguage={this.state.activeLanguage}
          apiContextPath={this.props.apiContextPath}
          debug={this.props.debug}
          defaultLanguage={this.state.defaultLanguage}
          isOpen={this.state.modalsOpen.imageAdder}
          onToggle={() => this.toggleModal('imageAdder')}
        />
        <LanguageAdderModal
          availableLanguages={this.state.availableLanguages}
          isOpen={this.state.modalsOpen.languageAdder}
          onClick={(language) => this.addLanguage(language, 'languageAdder')}
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
