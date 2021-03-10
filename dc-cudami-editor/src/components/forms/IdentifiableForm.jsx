import kebabCase from 'lodash/kebabCase'
import React, {Component} from 'react'
import {withTranslation} from 'react-i18next'

import '../common.css'
import ArticleForm from './ArticleForm'
import CollectionForm from './CollectionForm'
import CorporateBodyForm from './CorporateBodyForm'
import FileResourceForm from './FileResourceForm'
import FormErrors from '../FormErrors'
import ProjectForm from './ProjectForm'
import TopicForm from './TopicForm'
import WebpageForm from './WebpageForm'
import WebsiteForm from './WebsiteForm'
import AppContext from '../AppContext'
import {
  loadAvailableLanguages,
  loadDefaultLanguage,
  loadIdentifiable,
  saveIdentifiable,
  updateIdentifiable,
} from '../../api'
import '../../polyfills'
import IframeAdderModal from '../modals/IframeAdderModal'
import ImageAdderModal from '../modals/ImageAdderModal'
import LanguageAdderModal from '../modals/LanguageAdderModal'
import LinkAdderModal from '../modals/LinkAdderModal'
import PreviewImageAdderModal from '../modals/PreviewImageAdderModal'
import TableAdderModal from '../modals/TableAdderModal'
import VideoAdderModal from '../modals/VideoAdderModal'

class IdentifiableForm extends Component {
  identifiablesWithLongText = [
    'article',
    'collection',
    'corporateBody',
    'project',
    'webpage',
  ]

  identifiablesWithRenderingHints = ['webpage']

  constructor(props) {
    super(props)
    this.state = {
      activeLanguage: props.activeLanguage,
      availableLanguages: [],
      existingLanguages: props.existingLanguages ?? [props.activeLanguage],
      identifiable: null,
      invalidLanguages: [],
      modalsOpen: {
        iframeAdder: false,
        imageAdder: false,
        languageAdder: false,
        linkAdder: false,
        previewImageAdder: false,
        tableAdder: false,
        videoAdder: false,
      },
    }
  }

  async componentDidMount() {
    const {apiContextPath, mockApi, t, type, uuid} = this.props
    const availableLanguages = await loadAvailableLanguages(
      apiContextPath,
      mockApi
    )
    const defaultLanguage = await loadDefaultLanguage(apiContextPath, mockApi)
    let identifiable = await loadIdentifiable(
      apiContextPath,
      mockApi,
      type,
      uuid
    )
    const initialIdentifiable = {
      description: {},
      label: {
        [this.state.activeLanguage]: '',
      },
    }
    if (this.identifiablesWithRenderingHints.includes(type)) {
      initialIdentifiable.renderingHints = {showInPageNavigation: true}
    }
    if (this.identifiablesWithLongText.includes(type)) {
      initialIdentifiable.text = {}
    }
    identifiable = {
      ...initialIdentifiable,
      ...identifiable,
    }
    this.setState({
      availableLanguages: availableLanguages
        .reduce((languages, language) => {
          if (!(language in identifiable.label)) {
            languages.push({
              displayName: t(`languageNames:${language}`),
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

  addLanguage = (selectedLanguage) => {
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
      corporateBody: CorporateBodyForm,
      fileResource: FileResourceForm,
      project: ProjectForm,
      topic: TopicForm,
      webpage: WebpageForm,
      website: WebsiteForm,
    }
    const FormComponent = FORM_COMPONENT_MAPPING[this.props.type]
    return (
      <FormComponent
        activeLanguage={this.state.activeLanguage}
        canAddLanguage={this.state.availableLanguages.length > 0}
        existingLanguages={this.state.existingLanguages}
        formId={`${kebabCase(this.props.type)}-form`}
        identifiable={this.state.identifiable}
        onAddLanguage={() => this.toggleModal('languageAdder')}
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
      identifiable: {
        ...this.state.identifiable,
        ...identifiable,
      },
    })
  }

  render() {
    const {apiContextPath, mockApi, uiLocale} = this.props
    return this.state.identifiable ? (
      <AppContext.Provider
        value={{
          apiContextPath,
          defaultLanguage: this.state.defaultLanguage,
          mockApi,
          uiLocale,
        }}
      >
        <div className="identifiable-editor">
          {this.state.invalidLanguages.length > 0 && (
            <FormErrors invalidLanguages={this.state.invalidLanguages} />
          )}
          {this.getFormComponent()}
          <IframeAdderModal
            isOpen={this.state.modalsOpen.iframeAdder}
            onToggle={() => this.toggleModal('iframeAdder')}
          />
          <ImageAdderModal
            activeLanguage={this.state.activeLanguage}
            isOpen={this.state.modalsOpen.imageAdder}
            onToggle={() => this.toggleModal('imageAdder')}
          />
          <LanguageAdderModal
            addLanguage={this.addLanguage}
            availableLanguages={this.state.availableLanguages}
            isOpen={this.state.modalsOpen.languageAdder}
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
          <VideoAdderModal
            activeLanguage={this.state.activeLanguage}
            isOpen={this.state.modalsOpen.videoAdder}
            onToggle={() => this.toggleModal('videoAdder')}
          />
          <PreviewImageAdderModal
            activeLanguage={this.state.activeLanguage}
            isOpen={this.state.modalsOpen.previewImageAdder}
            onToggle={() => this.toggleModal('previewImageAdder')}
          />
        </div>
      </AppContext.Provider>
    ) : null
  }
}

IdentifiableForm.defaultProps = {
  apiContextPath: '/',
  mockApi: false,
}

export default withTranslation()(IdentifiableForm)
