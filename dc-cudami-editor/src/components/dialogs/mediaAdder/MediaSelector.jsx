import classNames from 'classnames'
import {useContext, useState} from 'react'
import {useTranslation} from 'react-i18next'
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
import {getLabelValue, getMediaUrl} from '../../utils'
import MediaLabelInput from './MediaLabelInput'

const upload = async (apiContextPath, file, mediaType, updateProgress) => {
  try {
    const response = await uploadFile(apiContextPath, file, updateProgress)
    return {
      message: {
        color: 'success',
        key: 'uploadSuccessful',
        values: {mediaType},
      },
      response: {
        ...response,
        uri: getMediaUrl(response, mediaType),
      },
    }
  } catch (err) {
    return {
      message: {
        color: 'danger',
        key: 'uploadFailed',
        values: {
          error: err,
        },
      },
    }
  }
}

const MediaSelector = ({
  activeLanguage,
  fileResource: {filename, label, previewImage, uri, uuid},
  mediaType,
  onChange,
  onTabChange,
}) => {
  const {apiContextPath, defaultLanguage} = useContext(AppContext)
  const [activeTab, setActiveTab] = useState('upload')
  const [loading, setLoading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [feedbackMessage, setFeedbackMessage] = useState()
  const {t} = useTranslation()
  const toggleTab = (tab, evt) => {
    if (tab !== activeTab && evt.currentTarget.id === evt.target.id) {
      onTabChange()
      setActiveTab(tab)
      setFeedbackMessage(undefined)
    }
  }
  const updateLabel = (newLabel, isUpdate = false) => {
    onChange(
      {
        label: {
          [Object.keys(label)[0]]: newLabel,
        },
      },
      isUpdate,
    )
  }
  return (
    <Card className="media-adder-content">
      <CardHeader className="font-weight-bold">
        <Nav className="card-header-tabs" tabs>
          <NavItem>
            <NavLink
              className={classNames({
                active: activeTab === 'upload',
              })}
              disabled={loading}
              href="#"
              id="upload"
              onClick={(evt) => toggleTab('upload', evt)}
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
              disabled={loading}
              href="#"
              id="url"
              onClick={(evt) => toggleTab('url', evt)}
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
              disabled={loading}
              href="#"
              id="search"
              onClick={(evt) => toggleTab('search', evt)}
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
            {uuid && (
              <PreviewImage
                className="mx-auto"
                image={previewImage}
                renderingHints={{
                  caption: {
                    [defaultLanguage]: label
                      ? getLabelValue(label, activeLanguage, defaultLanguage)
                      : filename,
                  },
                }}
                showCaption={true}
                width={250}
              />
            )}
            {feedbackMessage && (
              <FeedbackMessage
                message={feedbackMessage}
                onClose={() => setFeedbackMessage(undefined)}
              />
            )}
            <FileUploadForm
              onChange={async (file) => {
                setLoading(true)
                const {message, response} = await upload(
                  apiContextPath,
                  file,
                  mediaType,
                  setProgress,
                )
                setFeedbackMessage(message)
                if (response) {
                  onChange(response)
                }
                setLoading(false)
              }}
              progress={progress}
            />
            <MediaLabelInput
              activeLanguage={activeLanguage}
              className="mt-3"
              defaultLanguage={defaultLanguage}
              label={label}
              name={`${mediaType}-label-upload`}
              onChange={(label) => updateLabel(label, true)}
            />
          </TabPane>
          <TabPane tabId="url">
            {uri && (
              <PreviewImage
                className="mx-auto"
                image={{uri}}
                renderingHints={{
                  altText: {[defaultLanguage]: `${t(mediaType)}: ${uri}`},
                }}
                width={250}
              />
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
              activeLanguage={activeLanguage}
              defaultLanguage={defaultLanguage}
              label={label}
              name={`${mediaType}-label-url`}
              onChange={updateLabel}
            />
          </TabPane>
          <TabPane tabId="search">
            {uuid && (
              <PreviewImage
                className="mx-auto"
                image={previewImage}
                renderingHints={{
                  caption: {
                    [defaultLanguage]: label
                      ? getLabelValue(label, activeLanguage, defaultLanguage)
                      : filename,
                  },
                }}
                showCaption={true}
                width={250}
              />
            )}
            <Autocomplete
              activeLanguage={activeLanguage}
              onSearch={(contextPath, searchTerm, pageNumber, pageSize) =>
                searchMedia(contextPath, mediaType, {
                  pageNumber,
                  pageSize,
                  searchTerm,
                  sorting: {
                    orders: [{property: 'label', subProperty: defaultLanguage}],
                  },
                })
              }
              onSelect={(suggestion) => {
                onChange({
                  ...suggestion,
                  uri: getMediaUrl(suggestion, mediaType),
                })
              }}
              placeholder={t('selectMedia.searchTerm', {mediaType})}
            />
          </TabPane>
        </TabContent>
      </CardBody>
    </Card>
  )
}

export default MediaSelector
