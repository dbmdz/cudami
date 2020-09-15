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

import Autocomplete from '../../Autocomplete'
import ImageLabelInput from './ImageLabelInput'
import ImagePreview from './ImagePreview'
import AppContext from '../../AppContext'
import FeedbackMessage from '../../FeedbackMessage'
import FileUploadForm from '../../FileUploadForm'
import {getImageUrl} from '../../utils'
import {searchImages, uploadFile} from '../../../api'

class ImageSelector extends Component {
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

  uploadImage = async (image) => {
    this.setState({
      tabToggleEnabled: false,
    })
    const response = await uploadFile(
      this.context.apiContextPath,
      image,
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
      onChange,
      t,
      toggleTooltip,
      tooltipsOpen,
    } = this.props
    return (
      <Card className="mb-2">
        <CardHeader className="font-weight-bold">
          <Nav className="card-header-tabs" tabs>
            <NavItem>
              <NavLink
                className={classNames({
                  active: this.state.activeTab === 'upload',
                })}
                disabled={!this.state.tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('upload', evt)}
              >
                {t('selectImage.useUpload')}
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
                className={classNames({active: this.state.activeTab === 'url'})}
                disabled={!this.state.tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('url', evt)}
              >
                {t('selectImage.useUrl')}
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
                  active: this.state.activeTab === 'search',
                })}
                disabled={!this.state.tabToggleEnabled}
                href="#"
                onClick={(evt) => this.toggleTab('search', evt)}
              >
                {t('selectImage.useSearch')}
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
          <TabContent activeTab={this.state.activeTab} className="border-0 p-0">
            <TabPane tabId="upload">
              {fileResource.uuid && (
                <ImagePreview
                  iiifBaseUrl={fileResource.iiifBaseUrl}
                  filename={fileResource.filename}
                  mimeType={fileResource.mimeType}
                  uri={fileResource.uri}
                />
              )}
              {this.state.showUploadSuccess && (
                <FeedbackMessage
                  message={{
                    color: 'success',
                    key: 'selectImage.uploadSuccessful',
                  }}
                />
              )}
              <FileUploadForm
                onChange={(file) => this.uploadImage(file)}
                progress={this.state.progress}
              />
              <ImageLabelInput
                className="mt-3"
                label={fileResource.label}
                name="label-upload"
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
              {fileResource.uri && <ImagePreview uri={fileResource.uri} />}
              <FormGroup>
                <Input
                  name="url"
                  onChange={(evt) => onChange({uri: evt.target.value.trim()})}
                  pattern="(https?://|/).+?"
                  placeholder="URL"
                  required
                  type="text"
                  value={fileResource.uri}
                />
              </FormGroup>
              <ImageLabelInput
                label={fileResource.label}
                name="label-url"
                onChange={this.updateLabel}
                toggleTooltip={toggleTooltip}
                tooltipName="labelUrl"
                tooltipsOpen={tooltipsOpen}
              />
            </TabPane>
            <TabPane tabId="search">
              {fileResource.uuid && (
                <ImagePreview
                  iiifBaseUrl={fileResource.iiifBaseUrl}
                  filename={fileResource.filename}
                  mimeType={fileResource.mimeType}
                  uri={fileResource.uri}
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
                placeholder={t('selectImage.searchTerm')}
                search={searchImages}
              />
            </TabPane>
          </TabContent>
        </CardBody>
      </Card>
    )
  }
}

ImageSelector.contextType = AppContext

export default withTranslation()(ImageSelector)
