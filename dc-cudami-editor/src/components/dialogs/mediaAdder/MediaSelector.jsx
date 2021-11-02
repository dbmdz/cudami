import classNames from 'classnames'
import {Component} from 'react'
import {withTranslation} from 'react-i18next'
import {
  Card,
  CardBody,
  CardHeader,
  FormGroup,
  Input,
  Nav,
  NavItem,
  NavLink,
  TabContent,
  TabPane,
} from 'reactstrap'

import {searchMedia, uploadFile} from '../../../api'
import AppContext from '../../AppContext'
import Autocomplete from '../../Autocomplete'
import FeedbackMessage from '../../FeedbackMessage'
import FileUploadForm from '../../FileUploadForm'
import InfoTooltip from '../../InfoTooltip'
import PreviewImage from '../../PreviewImage'
import {getImageUrl, getVideoUrl} from '../../utils'
import MediaLabelInput from './MediaLabelInput'

class MediaSelector extends Component {
  constructor(props) {
    super(props)
    this.state = {
      activeTab: 'upload',
      progress: 0,
      tabToggleEnabled: true,
    }
  }

  getMediaUrl = (fileResource, mediaType) => {
    switch (mediaType) {
      case 'image':
        return getImageUrl(fileResource)
      case 'video':
        return getVideoUrl(fileResource)
      default:
        return null
    }
  }

  toggleTab = (activeTab, evt) => {
    if (
      activeTab !== this.state.activeTab &&
      evt.currentTarget === evt.target
    ) {
      this.props.onTabChanged()
      this.setState({
        activeTab,
      })
    }
  }

  updateLabel = (newValue, isUpdate = false) => {
    this.props.onChange(
      {
        label: {
          [Object.keys(this.props.fileResource.label)[0]]: newValue,
        },
      },
      isUpdate,
    )
  }

  uploadFile = async (file) => {
    const {mediaType, onChange} = this.props
    this.setState({
      tabToggleEnabled: false,
    })
    let feedbackMessage = {
      color: 'success',
      key: 'uploadSuccessful',
      values: {mediaType},
    }
    try {
      const response = await uploadFile(
        this.context.apiContextPath,
        file,
        (progress) => this.setState({progress}),
      )
      onChange({
        ...response,
        uri: this.getMediaUrl(response, mediaType),
      })
    } catch (err) {
      feedbackMessage = {
        color: 'danger',
        key: 'uploadFailed',
      }
    } finally {
      this.setState({
        feedbackMessage,
        tabToggleEnabled: true,
      })
      setTimeout(() => this.setState({feedbackMessage: undefined}), 3000)
    }
  }

  render() {
    const {activeLanguage, fileResource, mediaType, onChange, t} = this.props
    const {activeTab, progress, feedbackMessage, tabToggleEnabled} = this.state
    const {label, previewImage, uri} = fileResource
    return (
      <Card className="media-adder-content">
        <CardHeader className="font-weight-bold">
          <Nav className="card-header-tabs" tabs>
            <NavItem>
              <NavLink
                className={classNames({
                  active: activeTab === 'upload',
                })}
                disabled={!tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('upload', evt)}
              >
                {t('selectMedia.useUpload', {mediaType})}
                <InfoTooltip
                  className="ml-1 p-0"
                  color="link"
                  name="upload"
                  text={t('tooltips.upload')}
                />
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                className={classNames({active: activeTab === 'url'})}
                disabled={!tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('url', evt)}
              >
                {t('selectMedia.useUrl', {mediaType})}
                <InfoTooltip
                  className="ml-1 p-0"
                  color="link"
                  name="url"
                  text={t('tooltips.url')}
                />
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                className={classNames({
                  active: activeTab === 'search',
                })}
                disabled={!tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('search', evt)}
              >
                {t('selectMedia.useSearch', {mediaType})}
                <InfoTooltip
                  className="ml-1 p-0"
                  color="link"
                  name="search"
                  text={t('tooltips.search')}
                />
              </NavLink>
            </NavItem>
          </Nav>
        </CardHeader>
        <CardBody className="text-center">
          <TabContent activeTab={activeTab} className="border-0 p-0">
            <TabPane tabId="upload">
              {previewImage && (
                <PreviewImage
                  className="mx-auto"
                  image={previewImage}
                  renderingHints={{
                    caption: {
                      [this.context.defaultLanguage]: previewImage.filename,
                    },
                  }}
                  showCaption={true}
                  width={250}
                />
              )}
              {feedbackMessage && <FeedbackMessage message={feedbackMessage} />}
              <FileUploadForm
                onChange={(file) => this.uploadFile(file)}
                progress={progress}
              />
              <MediaLabelInput
                className="mt-3"
                label={label}
                name={`${mediaType}-label-upload`}
                onChange={(label) => this.updateLabel(label, true)}
              />
            </TabPane>
            <TabPane tabId="url">
              {uri && (
                <PreviewImage className="mx-auto" image={{uri}} width={250} />
              )}
              <FormGroup>
                <Input
                  name="url"
                  onChange={(evt) => onChange({uri: evt.target.value.trim()})}
                  placeholder="URL"
                  required
                  type="url"
                  value={uri}
                />
              </FormGroup>
              <MediaLabelInput
                label={label}
                name={`${mediaType}-label-url`}
                onChange={this.updateLabel}
              />
            </TabPane>
            <TabPane tabId="search">
              {previewImage && (
                <PreviewImage
                  className="mx-auto"
                  image={previewImage}
                  renderingHints={{
                    caption: {
                      [this.context.defaultLanguage]: previewImage.filename,
                    },
                  }}
                  showCaption={true}
                  width={250}
                />
              )}
              <Autocomplete
                activeLanguage={activeLanguage}
                onSelect={(suggestion) => {
                  onChange({
                    ...suggestion,
                    uri: this.getMediaUrl(suggestion, mediaType),
                  })
                }}
                placeholder={t('selectMedia.searchTerm', {mediaType})}
                search={(contextPath, searchTerm, pageNumber, pageSize) =>
                  searchMedia(
                    contextPath,
                    mediaType,
                    searchTerm,
                    pageNumber,
                    pageSize,
                  )
                }
              />
            </TabPane>
          </TabContent>
        </CardBody>
      </Card>
    )
  }
}

MediaSelector.contextType = AppContext

export default withTranslation()(MediaSelector)
