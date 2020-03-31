import React, {Component} from 'react'
import {Card, CardBody, CardHeader, FormGroup, Input} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import FileUploadForm from '../../FileUploadForm'
import {uploadFile} from '../../../api'

class ImageSelector extends Component {
  constructor(props) {
    super(props)
    this.state = {
      progress: 0,
    }
  }

  updateProgress = (progress) => {
    this.setState({
      progress,
    })
  }

  uploadImage = async (image) => {
    const {apiContextPath, onChange} = this.props
    const response = await uploadFile(
      apiContextPath,
      image,
      this.updateProgress
    )
    const responseJson = JSON.parse(response)
    onChange(
      {
        label: responseJson.label,
        uri: `${responseJson.iiifBaseUrl}${responseJson.uuid}/full/full/0/default.${responseJson.filenameExtension}`,
        uuid: responseJson.uuid,
      },
      {
        metadataOpen: true,
        toggleEnabled: true,
      }
    )
  }

  render() {
    const {fileResource, onChange, t} = this.props
    return (
      <Card className="mt-0">
        <CardHeader>{t('selectImage')}</CardHeader>
        <CardBody className="text-center">
          <FileUploadForm
            onChange={(file) => this.uploadImage(file)}
            progress={this.state.progress}
          />
          <FormGroup className="mt-3">
            <Input
              name="url"
              onChange={(evt) =>
                onChange(
                  {uri: evt.target.value, uuid: undefined},
                  {metadataOpen: true, toggleEnabled: true}
                )
              }
              placeholder="URL"
              required
              type="url"
              value={fileResource.uri}
            />
          </FormGroup>
          <FormGroup>
            <Input
              name="label"
              onChange={(evt) =>
                onChange({
                  label: {
                    [Object.keys(fileResource.label)[0]]: evt.target.value,
                  },
                })
              }
              placeholder={t('label')}
              required
              type="text"
              value={
                fileResource.label ? Object.values(fileResource.label)[0] : ''
              }
            />
          </FormGroup>
        </CardBody>
      </Card>
    )
  }
}

export default withTranslation()(ImageSelector)
