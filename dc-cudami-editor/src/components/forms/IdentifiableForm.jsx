import '../../polyfills'

import kebabCase from 'lodash/kebabCase'
import React, {Component} from 'react'
import {withTranslation} from 'react-i18next'

import {
  loadAvailableLanguages,
  loadDefaultLanguage,
  loadIdentifiable,
  saveIdentifiable,
  updateIdentifiable,
} from '../../api'
import AppContext from '../AppContext'
import AddIframeDialog from '../dialogs/AddIframeDialog'
import AddImageDialog from '../dialogs/AddImageDialog'
import AddLanguageDialog from '../dialogs/AddLanguageDialog'
import AddLinkDialog from '../dialogs/AddLinkDialog'
import AddPreviewImageDialog from '../dialogs/AddPreviewImageDialog'
import AddTableDialog from '../dialogs/AddTableDialog'
import AddVideoDialog from '../dialogs/AddVideoDialog'
import FormErrors from '../FormErrors'
import ArticleForm from './ArticleForm'
import CollectionForm from './CollectionForm'
import CorporateBodyForm from './CorporateBodyForm'
import FileResourceForm from './FileResourceForm'
import ProjectForm from './ProjectForm'
import TopicForm from './TopicForm'
import WebpageForm from './WebpageForm'
import WebsiteForm from './WebsiteForm'

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
      dialogsOpen: {
        addIframe: false,
        addImage: false,
        addLanguage: false,
        addLink: false,
        addPreviewImage: false,
        addTable: false,
        addVideo: false,
      },
      existingLanguages: props.existingLanguages ?? [props.activeLanguage],
      identifiable: null,
      invalidLanguages: [],
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
        onAddLanguage={() => this.toggleDialog('addLanguage')}
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

  toggleDialog = (name) => {
    this.setState({
      dialogsOpen: {
        ...this.state.dialogsOpen,
        [name]: !this.state.dialogsOpen[name],
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
    const {
      activeLanguage,
      availableLanguages,
      defaultLanguage,
      dialogsOpen,
      invalidLanguages,
    } = this.state
    return this.state.identifiable ? (
      <AppContext.Provider
        value={{
          apiContextPath,
          defaultLanguage,
          mockApi,
          uiLocale,
        }}
      >
        <div className="identifiable-editor">
          {invalidLanguages.length > 0 && (
            <FormErrors invalidLanguages={invalidLanguages} />
          )}
          {this.getFormComponent()}
          <AddIframeDialog
            isOpen={dialogsOpen.addIframe}
            onToggle={() => this.toggleDialog('addIframe')}
          />
          <AddImageDialog
            activeLanguage={activeLanguage}
            isOpen={dialogsOpen.addImage}
            onToggle={() => this.toggleDialog('addImage')}
          />
          <AddLanguageDialog
            addLanguage={this.addLanguage}
            availableLanguages={availableLanguages}
            isOpen={dialogsOpen.addLanguage}
            onToggle={() => this.toggleDialog('addLanguage')}
          />
          <AddLinkDialog
            isOpen={dialogsOpen.addLink}
            onToggle={() => this.toggleDialog('addLink')}
          />
          <AddTableDialog
            isOpen={dialogsOpen.addTable}
            onToggle={() => this.toggleDialog('addTable')}
          />
          <AddVideoDialog
            activeLanguage={activeLanguage}
            isOpen={dialogsOpen.addVideo}
            onToggle={() => this.toggleDialog('addVideo')}
          />
          <AddPreviewImageDialog
            activeLanguage={activeLanguage}
            isOpen={dialogsOpen.addPreviewImage}
            onToggle={() => this.toggleDialog('addPreviewImage')}
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
