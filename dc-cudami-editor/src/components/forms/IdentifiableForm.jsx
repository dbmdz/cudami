import '../../polyfills'

import kebabCase from 'lodash/kebabCase'
import omit from 'lodash/omit'
import sortBy from 'lodash/sortBy'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'

import {
  loadAvailableLanguages,
  loadDefaultLanguage,
  loadIdentifiable,
  saveIdentifiable,
  typeToEndpointMapping,
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
import RemoveLanguageDialog from '../dialogs/RemoveLanguageDialog'
import RemoveUrlAliasDialog from '../dialogs/RemoveUrlAliasDialog'
import FeedbackMessage from '../FeedbackMessage'
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
        removeLanguage: false,
        removeUrlAlias: false,
      },
      existingLanguages: props.existingLanguages ?? [props.activeLanguage],
      identifiable: null,
      invalidLanguages: props.existingLanguages ? [] : [props.activeLanguage],
    }
  }

  async componentDidMount() {
    const {apiContextPath, t, type, uuid} = this.props
    const availableLanguages = await loadAvailableLanguages(apiContextPath)
    const defaultLanguage = await loadDefaultLanguage(apiContextPath)
    let identifiable = await loadIdentifiable(apiContextPath, type, uuid)
    const initialIdentifiable = {
      description: {},
      label: {
        [this.state.activeLanguage]: '',
      },
      localizedUrlAliases: {
        [this.state.activeLanguage]: [],
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
    const {
      availableLanguages,
      existingLanguages,
      identifiable,
      invalidLanguages,
    } = this.state
    this.setState({
      activeLanguage: selectedLanguage.name,
      availableLanguages: availableLanguages.filter(
        (language) => language.name !== selectedLanguage.name,
      ),
      existingLanguages: [...existingLanguages, selectedLanguage.name],
      identifiable: {
        ...identifiable,
        label: {
          ...identifiable.label,
          [selectedLanguage.name]: '',
        },
        localizedUrlAliases: {
          ...identifiable.localizedUrlAliases,
          [selectedLanguage.name]: [],
        },
      },
      invalidLanguages: [...invalidLanguages, selectedLanguage.name],
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
      {},
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
    const {
      activeLanguage,
      availableLanguages,
      existingLanguages,
      identifiable,
      invalidLanguages,
    } = this.state
    return (
      <FormComponent
        activeLanguage={activeLanguage}
        canAddLanguage={availableLanguages.length > 0}
        existingLanguages={existingLanguages}
        formId={`${kebabCase(this.props.type)}-form`}
        identifiable={identifiable}
        invalidLanguages={invalidLanguages}
        onAddLanguage={() => this.toggleDialog('addLanguage')}
        onSubmit={this.submitIdentifiable}
        onToggleLanguage={this.toggleLanguage}
        onUpdate={this.updateIdentifiable}
      />
    )
  }

  getInvalidLanguages = (identifiable) =>
    this.state.existingLanguages.filter((l) => !identifiable.label[l])

  isEmptyContent = (content) => {
    return (
      content.length === 1 &&
      content[0].type === 'paragraph' &&
      !content[0].content
    )
  }

  removeLanguage = (language) => {
    const {
      activeLanguage,
      availableLanguages,
      existingLanguages,
      identifiable,
    } = this.state
    const newState = {
      availableLanguages: sortBy(
        [
          ...availableLanguages,
          {
            displayName: this.props.t(`languageNames:${language}`),
            name: language,
          },
        ],
        'displayName',
      ),
      existingLanguages: existingLanguages.filter((l) => l != language),
      identifiable: {
        ...identifiable,
        description: omit(identifiable.description, [language]),
        label: omit(identifiable.label, [language]),
      },
    }
    if (language === activeLanguage) {
      newState.activeLanguage = newState.existingLanguages[0]
    }
    if (identifiable.text) {
      newState.identifiable.text = omit(identifiable.text, [language])
    }
    this.setState(newState)
  }

  removeUrlAlias = (slug, websiteUuid) => {
    const {activeLanguage, identifiable} = this.state
    this.updateIdentifiable({
      localizedUrlAliases: {
        ...identifiable.localizedUrlAliases,
        [activeLanguage]: identifiable.localizedUrlAliases[
          activeLanguage
        ].filter(
          (alias) =>
            !(alias.slug === slug && alias.website?.uuid === websiteUuid),
        ),
      },
    })
  }

  submitIdentifiable = async () => {
    const {apiContextPath, parentType, parentUuid, type} = this.props
    const identifiable = {
      ...this.state.identifiable,
      description: this.cleanUpJson(this.state.identifiable.description),
    }
    if (identifiable.text) {
      identifiable.text = this.cleanUpJson(this.state.identifiable.text)
    }
    const {error = false, uuid} = await (identifiable.uuid
      ? updateIdentifiable(apiContextPath, identifiable, type)
      : saveIdentifiable(apiContextPath, identifiable, type, {
          parentType,
          parentUuid,
        }))
    if (error) {
      return this.setState({
        feedbackMessage: {
          color: 'danger',
          key: 'submitOfFormFailed',
        },
      })
    }
    window.location.href = `${apiContextPath}${typeToEndpointMapping[type]}/${uuid}`
  }

  toggleDialog = (name) => {
    this.setState({
      dialogsOpen: {
        ...this.state.dialogsOpen,
        [name]: !this.state.dialogsOpen[name],
      },
    })
  }

  toggleLanguage = (activeLanguage) => {
    this.setState({
      activeLanguage,
    })
  }

  updateIdentifiable = (identifiable) => {
    const newState = {
      identifiable: {
        ...this.state.identifiable,
        ...identifiable,
      },
    }
    if (identifiable.label) {
      newState.invalidLanguages = this.getInvalidLanguages(identifiable)
    }
    this.setState(newState)
  }

  render() {
    const {apiContextPath, type, uiLocale} = this.props
    const {
      activeLanguage,
      availableLanguages,
      defaultLanguage,
      dialogsOpen,
      feedbackMessage,
      identifiable,
      invalidLanguages,
    } = this.state
    if (!identifiable) {
      return null
    }
    const shouldNotRenderLabelWarning =
      !invalidLanguages.length ||
      /* FIXME: little hack for the FileResourceUploadForm, there will never be a filled label */
      (type === 'fileResource' && !identifiable.uuid)
    return (
      <AppContext.Provider
        value={{
          apiContextPath,
          defaultLanguage,
          uiLocale,
        }}
      >
        <div className="identifiable-editor">
          {shouldNotRenderLabelWarning || (
            <FeedbackMessage
              className="mb-2"
              message={{color: 'warning', key: 'labelNotFilled'}}
            />
          )}
          {feedbackMessage && (
            <FeedbackMessage
              className="mb-2"
              message={feedbackMessage}
              onClose={() => this.setState({feedbackMessage: undefined})}
            />
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
          <RemoveLanguageDialog
            isOpen={dialogsOpen.removeLanguage}
            onConfirm={this.removeLanguage}
            toggle={() => this.toggleDialog('removeLanguage')}
          />
          <RemoveUrlAliasDialog
            activeLanguage={activeLanguage}
            isOpen={dialogsOpen.removeUrlAlias}
            onConfirm={this.removeUrlAlias}
            toggle={() => this.toggleDialog('removeUrlAlias')}
          />
        </div>
      </AppContext.Provider>
    )
  }
}

IdentifiableForm.defaultProps = {
  apiContextPath: '/',
}

export default withTranslation()(IdentifiableForm)
