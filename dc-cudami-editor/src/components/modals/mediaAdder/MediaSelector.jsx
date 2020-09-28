import React, {Component} from 'react'
import classNames from 'classnames'
import {
  Card,
  CardBody,
  CardHeader,
  FormGroup,
  Input,
  Nav,
  NavItem,
  NavLink,
  PopoverBody,
  TabContent,
  TabPane,
  UncontrolledPopover,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

import MediaLabelInput from './MediaLabelInput'
import AppContext from '../../AppContext'
import Autocomplete from '../../Autocomplete'
import FeedbackMessage from '../../FeedbackMessage'
import FileUploadForm from '../../FileUploadForm'
import PreviewImage from '../../PreviewImage'
import {getImageUrl, getVideoUrl} from '../../utils'
import {searchMedia, uploadFile} from '../../../api'

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

  updateLabel = (newValue, additionalFields = {}) => {
    this.props.onChange(
      {
        label: {
          [Object.keys(this.props.fileResource.label)[0]]: newValue,
        },
      },
      additionalFields
    )
  }

  uploadFile = async (file) => {
    const {mediaType, onChange} = this.props
    this.setState({
      tabToggleEnabled: false,
    })
    let feedbackMessage = {
      color: 'success',
      key: 'selectMedia.uploadSuccessful',
      values: {mediaType},
    }
    try {
      const response = await uploadFile(
        this.context.apiContextPath,
        file,
        this.context.mockApi,
        (progress) => this.setState({progress})
      )
      onChange({
        ...response,
        uri: this.getMediaUrl(response, mediaType),
      })
    } catch (err) {
      console.log(err)
      feedbackMessage = {
        color: 'danger',
        key: 'selectMedia.uploadFailed',
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
                <FaQuestionCircle className="ml-1" id="upload-tooltip" />
                <UncontrolledPopover
                  placement="top"
                  target="upload-tooltip"
                  trigger="focus"
                >
                  <PopoverBody>{t('tooltips.upload')}</PopoverBody>
                </UncontrolledPopover>
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
                <FaQuestionCircle className="ml-1" id="url-tooltip" />
                <UncontrolledPopover
                  placement="top"
                  target="url-tooltip"
                  trigger="focus"
                >
                  <PopoverBody>{t('tooltips.url')}</PopoverBody>
                </UncontrolledPopover>
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
                <FaQuestionCircle className="ml-1" id="search-tooltip" />
                <UncontrolledPopover
                  placement="top"
                  target="search-tooltip"
                  trigger="focus"
                >
                  <PopoverBody>{t('tooltips.search')}</PopoverBody>
                </UncontrolledPopover>
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
                onChange={(label) =>
                  this.updateLabel(label, {
                    doUpdateRequest: true,
                  })
                }
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
                  pattern="(https?://|/).+?"
                  placeholder="URL"
                  required
                  type="text"
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
                search={(contextPath, mock, searchTerm, pageNumber, pageSize) =>
                  searchMedia(
                    contextPath,
                    mediaType,
                    mock,
                    searchTerm,
                    pageNumber,
                    pageSize
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
