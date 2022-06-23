import '../../polyfills'

import groupBy from 'lodash/groupBy'
import kebabCase from 'lodash/kebabCase'
import mergeWith from 'lodash/mergeWith'
import omit from 'lodash/omit'
import pick from 'lodash/pick'
import sortBy from 'lodash/sortBy'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'

import {
  generateSlug,
  getAvailableLanguages,
  getByUuid,
  getConfig,
  save,
  typeToEndpointMapping,
  update,
} from '../../api'
import AppContext from '../AppContext'
import AddIframeDialog from '../dialogs/AddIframeDialog'
import AddLanguageDialog from '../dialogs/AddLanguageDialog'
import AddLinkDialog from '../dialogs/AddLinkDialog'
import AddMediaDialog from '../dialogs/AddMediaDialog'
import AddTableDialog from '../dialogs/AddTableDialog'
import AddUrlAliasesDialog from '../dialogs/AddUrlAliasesDialog'
import ConfirmGeneratatedUrlAliasesDialog from '../dialogs/ConfirmGeneratatedUrlAliasesDialog'
import RemoveLanguageDialog from '../dialogs/RemoveLanguageDialog'
import RemoveUrlAliasDialog from '../dialogs/RemoveUrlAliasDialog'
import SetPreviewImageDialog from '../dialogs/SetPreviewImageDialog'
import FeedbackMessage from '../FeedbackMessage'
import ArticleForm from './ArticleForm'
import CollectionForm from './CollectionForm'
import CorporateBodyForm from './CorporateBodyForm'
import FileResourceForm from './FileResourceForm'
import ProjectForm from './ProjectForm'
import TopicForm from './TopicForm'
import WebpageForm from './WebpageForm'
import WebsiteForm from './WebsiteForm'

const typeToClassNamesMapping = {
  article: [{className: 'Article'}],
  collection: [{className: 'Collection'}],
  corporateBody: [{className: 'CorporateBody'}],
  fileResource: [
    {className: 'ApplicationFileResource', mimeType: 'application/'},
    {className: 'AudioFileResource', mimeType: 'audio/'},
    {className: 'ImageFileResource', mimeType: 'image/'},
    {className: 'LinkedDataFileResource', mimeType: 'application/'},
    {className: 'TextFileResource', mimeType: 'text/'},
    {className: 'VideoFileResource', mimeType: 'video/'},
  ],
  project: [{className: 'Project'}],
  topic: [{className: 'Topic'}],
  webpage: [{className: 'Webpage'}],
  website: [{className: 'Website'}],
}

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
        addTable: false,
        addUrlAliases: false,
        addVideo: false,
        confirmGeneratatedUrlAliases: false,
        removeLanguage: false,
        removeUrlAlias: false,
        setPreviewImage: false,
      },
      existingLanguages: props.existingLanguages ?? [props.activeLanguage],
      identifiable: null,
      initialFileResource: null,
      invalidLanguages: props.existingLanguages ? [] : [props.activeLanguage],
    }
  }

  async componentDidMount() {
    const {apiContextPath, t, type, uuid} = this.props
    const availableLanguages = await getAvailableLanguages(apiContextPath)
    const {
      defaults: {language: defaultLanguage},
      urlAlias: {generationExcludes},
    } = await getConfig(apiContextPath)
    const identifiable = await getByUuid(apiContextPath, type, uuid)
    const initialIdentifiable = {
      description: {},
      label: {
        [this.state.activeLanguage]: '',
      },
      localizedUrlAliases: {
        [this.state.activeLanguage]: [],
      },
      renderingHints: this.identifiablesWithRenderingHints.includes(type)
        ? {
            showInPageNavigation: true,
          }
        : undefined,
      text: this.identifiablesWithLongText.includes(type) ? {} : undefined,
      ...identifiable,
    }
    const initialFileResource = await getByUuid(apiContextPath, 'fileResource')
    this.setState({
      availableLanguages: availableLanguages
        .filter((language) => !(language in initialIdentifiable.label))
        .map((language) => ({
          displayName: t(`languageNames:${language}`),
          name: language,
        }))
        .sort((a, b) => (a.displayName > b.displayName ? 1 : -1)),
      defaultLanguage,
      generationExcludes,
      identifiable: initialIdentifiable,
      initialFileResource,
      initialLabel: initialIdentifiable.label,
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

  addUrlAlias = (newUrlAlias) => {
    const {activeLanguage, identifiable} = this.state
    this.updateIdentifiable({
      localizedUrlAliases: {
        ...identifiable.localizedUrlAliases,
        [activeLanguage]: [
          ...(identifiable.localizedUrlAliases[activeLanguage] ?? []),
          newUrlAlias,
        ],
      },
    })
  }

  /*
   * Removes languages with empty content from the json
   */
  cleanContent = (localizedContent) => {
    const cleanedContent = Object.entries(localizedContent).filter(
      ([, doc]) => !this.isEmptyContent(doc.content),
    )
    if (!cleanedContent.length) {
      return
    }
    return Object.fromEntries(cleanedContent)
  }

  /*
   * Removes languages with empty list of aliases from the json
   */
  cleanUrlAliases = (localizedAliases) => {
    const cleanedAliases = Object.entries(localizedAliases).filter(
      ([, aliases]) => aliases.length,
    )
    if (!cleanedAliases.length) {
      return
    }
    return Object.fromEntries(cleanedAliases)
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
        onSubmit={this.validateUrlAliases}
        onToggleLanguage={this.toggleLanguage}
        onUpdate={this.updateIdentifiable}
      />
    )
  }

  getGeneratedUrlAliasForLanguageAndWebsite = async (language, website) => {
    const {apiContextPath, uuid} = this.props
    const {identifiableObjectType, label, type} = this.state.identifiable
    const slug = await generateSlug(
      apiContextPath,
      language,
      label[language],
      website?.uuid,
    )
    return {
      primary: true,
      slug,
      targetIdentifiableObjectType: identifiableObjectType,
      targetIdentifiableType: type,
      targetLanguage: language,
      targetUuid: uuid,
      website:
        website &&
        pick(website, ['identifiableObjectType', 'type', 'url', 'uuid']),
    }
  }

  getGeneratedUrlAliases = async () => {
    const {parentWebsite, type} = this.props
    const {
      existingLanguages,
      generationExcludes,
      identifiable: {label, localizedUrlAliases, mimeType: mime},
      initialLabel,
    } = this.state
    if (
      typeToClassNamesMapping[type].some(
        ({className, mimeType}) =>
          (mimeType ? mime?.startsWith(mimeType) : true) &&
          generationExcludes.includes(className),
      )
    ) {
      return {}
    }
    const languagesWithoutGeneratedUrlAliases = existingLanguages.filter(
      (language) => {
        const listOfAliases = localizedUrlAliases[language]
        /* filter the aliases that are connected with the parent website */
        const existingDefaultUrlAlias = listOfAliases.filter(
          ({website}) => website?.uuid === parentWebsite?.uuid,
        )
        const labelChanged =
          initialLabel?.[language] &&
          initialLabel?.[language] !== label[language]
        return !existingDefaultUrlAlias.length || labelChanged
      },
    )
    const generatedUrlAliases = languagesWithoutGeneratedUrlAliases.reduce(
      (aliases, language) => ({
        ...aliases,
        [language]: [],
      }),
      {},
    )
    for (let language of languagesWithoutGeneratedUrlAliases) {
      const listOfAliases = localizedUrlAliases[language]
      /* filter the aliases that are connected with the parent website */
      const existingDefaultUrlAlias = listOfAliases.filter(
        ({website}) => website?.uuid === parentWebsite?.uuid,
      )
      /* add an alias with the parent website if there are no existing aliases for the language */
      if (!existingDefaultUrlAlias.length) {
        const newAlias = await this.getGeneratedUrlAliasForLanguageAndWebsite(
          language,
          parentWebsite,
        )
        generatedUrlAliases[language].push(newAlias)
        continue
      }
      /* extract the websites with connected aliases */
      const existingWebsites = Object.values(
        groupBy(listOfAliases, 'website.uuid'),
      ).map((aliases) => aliases[0].website)
      for (let website of existingWebsites) {
        const newAlias = await this.getGeneratedUrlAliasForLanguageAndWebsite(
          language,
          website,
        )
        /* find possibly manually added duplicates that are not yet persisted */
        const duplicate = listOfAliases.find(
          (alias) =>
            alias.slug === newAlias.slug &&
            alias.website?.uuid === website?.uuid,
        )
        if (!duplicate) {
          generatedUrlAliases[language].push(newAlias)
        }
      }
    }
    return generatedUrlAliases
  }

  getInvalidLanguages = (identifiable) =>
    this.state.existingLanguages.filter((l) => !identifiable.label[l])

  isEmptyContent = (content) =>
    content.length === 1 &&
    content[0].type === 'paragraph' &&
    !content[0].content

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
        localizedUrlAliases: omit(identifiable.localizedUrlAliases, [language]),
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
      description: this.cleanContent(this.state.identifiable.description),
      localizedUrlAliases: this.cleanUrlAliases(
        this.state.identifiable.localizedUrlAliases,
      ),
    }
    if (this.state.generatedUrlAliases) {
      identifiable.localizedUrlAliases = mergeWith(
        identifiable.localizedUrlAliases,
        this.state.generatedUrlAliases,
        (objValue, srcValue) => {
          return (objValue ?? []).concat(srcValue)
        },
      )
    }
    if (identifiable.text) {
      identifiable.text = this.cleanContent(identifiable.text)
    }
    const {error = false, uuid} = await (identifiable.uuid
      ? update(apiContextPath, identifiable, type)
      : save(apiContextPath, identifiable, type, {
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

  validateUrlAliases = async () => {
    const generatedUrlAliases = await this.getGeneratedUrlAliases()
    if (Object.keys(generatedUrlAliases).length > 0) {
      return this.setState({
        dialogsOpen: {
          ...this.state.dialogsOpen,
          confirmGeneratatedUrlAliases: true,
        },
        generatedUrlAliases,
      })
    }
    this.submitIdentifiable()
  }

  render() {
    const {apiContextPath, parentWebsite, type, uiLocale, uuid} = this.props
    const {
      activeLanguage,
      availableLanguages,
      defaultLanguage,
      dialogsOpen,
      feedbackMessage,
      generatedUrlAliases,
      identifiable,
      initialFileResource,
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
            toggle={() => this.toggleDialog('addIframe')}
          />
          <AddLanguageDialog
            addLanguage={this.addLanguage}
            availableLanguages={availableLanguages}
            isOpen={dialogsOpen.addLanguage}
            toggle={() => this.toggleDialog('addLanguage')}
          />
          <AddLinkDialog
            isOpen={dialogsOpen.addLink}
            toggle={() => this.toggleDialog('addLink')}
          />
          <AddMediaDialog
            activeLanguage={activeLanguage}
            initialFileResource={initialFileResource}
            isOpen={dialogsOpen.addImage}
            mediaType="image"
            toggle={() => this.toggleDialog('addImage')}
          />
          <AddMediaDialog
            activeLanguage={activeLanguage}
            enableAltText={false}
            enableLink={false}
            enablePreviewImage={true}
            initialFileResource={initialFileResource}
            isOpen={dialogsOpen.addVideo}
            mediaType="video"
            toggle={() => this.toggleDialog('addVideo')}
          />
          <AddTableDialog
            isOpen={dialogsOpen.addTable}
            toggle={() => this.toggleDialog('addTable')}
          />
          <AddUrlAliasesDialog
            activeLanguage={activeLanguage}
            existingUrlAliases={
              identifiable.localizedUrlAliases[activeLanguage]
            }
            isOpen={dialogsOpen.addUrlAliases}
            onSubmit={this.addUrlAlias}
            parentWebsite={parentWebsite}
            target={{
              targetIdentifiableObjectType: identifiable.identifiableObjectType,
              targetIdentifiableType: identifiable.type,
              targetUuid: uuid,
            }}
            toggle={() => this.toggleDialog('addUrlAliases')}
          />
          <ConfirmGeneratatedUrlAliasesDialog
            generatedUrlAliases={generatedUrlAliases}
            isOpen={dialogsOpen.confirmGeneratatedUrlAliases}
            onChange={(generatedUrlAliases) =>
              this.setState({generatedUrlAliases})
            }
            onConfirm={this.submitIdentifiable}
            toggle={() => this.toggleDialog('confirmGeneratatedUrlAliases')}
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
          <SetPreviewImageDialog
            activeLanguage={activeLanguage}
            initialFileResource={initialFileResource}
            isOpen={dialogsOpen.setPreviewImage}
            toggle={() => this.toggleDialog('setPreviewImage')}
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
