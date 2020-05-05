import React, {Component} from 'react'
import {
  Card,
  CardBody,
  CardHeader,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
  Popover,
  PopoverBody,
} from 'reactstrap'
import {withTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

import FileUploadForm from '../../FileUploadForm'
import {uploadFile} from '../../../api'

class ImageSelector extends Component {
  constructor(props) {
    super(props)
    this.state = {
      labelTooltipOpen: false,
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
    onChange({
      label: responseJson.label,
      uri: `${responseJson.iiifBaseUrl}/full/full/0/default.${responseJson.filenameExtension}`,
      uuid: responseJson.uuid,
    })
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
                onChange({uri: evt.target.value, uuid: undefined})
              }
              placeholder="URL"
              required
              type="url"
              value={fileResource.uri}
            />
          </FormGroup>
          <FormGroup>
            <InputGroup>
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
              <InputGroupAddon addonType="append">
                <InputGroupText>
                  <FaQuestionCircle
                    id="label-tooltip"
                    style={{cursor: 'pointer'}}
                  />
                  <Popover
                    isOpen={this.state.labelTooltipOpen}
                    placement="left"
                    target="label-tooltip"
                    toggle={() =>
                      this.setState({
                        labelTooltipOpen: !this.state.labelTooltipOpen,
                      })
                    }
                  >
                    <PopoverBody>{t('tooltips.label')}</PopoverBody>
                  </Popover>
                </InputGroupText>
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
        </CardBody>
      </Card>
    )
  }
}

export default withTranslation()(ImageSelector)
