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
  Popover,
  PopoverBody,
  TabContent,
  TabPane,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

import MediaLabelInput from './MediaLabelInput'
import AppContext from '../../AppContext'
import Autocomplete from '../../Autocomplete'
import FeedbackMessage from '../../FeedbackMessage'
import FileUploadForm from '../../FileUploadForm'
import PreviewImage from '../../PreviewImage'
import {getImageUrl} from '../../utils'
import {searchMedia, uploadFile} from '../../../api'

class MediaSelector extends Component {
  constructor(props) {
    super(props)
    this.state = {
      activeTab: 'upload',
      progress: 0,
      showUploadSuccess: false,
      tabToggleEnabled: true,
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

  updateProgress = (progress) => {
    this.setState({
      progress,
    })
  }

  uploadFile = async (file) => {
    this.setState({
      tabToggleEnabled: false,
    })
    const response = await uploadFile(
      this.context.apiContextPath,
      file,
      this.context.mockApi,
      this.updateProgress
    )
    this.setState({
      tabToggleEnabled: true,
      showUploadSuccess: true,
    })
    setTimeout(() => this.setState({showUploadSuccess: false}), 3000)
    this.props.onChange({
      ...response,
      uri: getImageUrl(response),
    })
  }

  render() {
    const {
      activeLanguage,
      fileResource,
      mediaType,
      onChange,
      t,
      toggleTooltip,
      tooltipsOpen,
    } = this.props
    const {
      activeTab,
      progress,
      showUploadSuccess,
      tabToggleEnabled,
    } = this.state
    const {label, previewImage, uri} = fileResource
    return (
      <Card className="mb-2">
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
                <Popover
                  isOpen={tooltipsOpen.upload}
                  placement="top"
                  target="upload-tooltip"
                  toggle={() => toggleTooltip('upload')}
                >
                  <PopoverBody>{t('tooltips.upload')}</PopoverBody>
                </Popover>
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
                <Popover
                  isOpen={tooltipsOpen.url}
                  placement="top"
                  target="url-tooltip"
                  toggle={() => toggleTooltip('url')}
                >
                  <PopoverBody>{t('tooltips.url')}</PopoverBody>
                </Popover>
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
                <Popover
                  isOpen={tooltipsOpen.search}
                  placement="top"
                  target="search-tooltip"
                  toggle={() => toggleTooltip('search')}
                >
                  <PopoverBody>{t('tooltips.search')}</PopoverBody>
                </Popover>
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
              {showUploadSuccess && (
                <FeedbackMessage
                  message={{
                    color: 'success',
                    key: 'selectMedia.uploadSuccessful',
                    values: {mediaType},
                  }}
                />
              )}
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
                toggleTooltip={toggleTooltip}
                tooltipName="labelUpload"
                tooltipsOpen={tooltipsOpen}
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
                toggleTooltip={toggleTooltip}
                tooltipName="labelUrl"
                tooltipsOpen={tooltipsOpen}
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
                    ...suggestion.previewImage,
                    ...suggestion,
                    uri: getImageUrl(suggestion.previewImage),
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
