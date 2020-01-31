import React, {Component} from 'react'
import {Button, Form} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import FileUploadForm from '../../FileUploadForm'
import {uploadFile} from '../../../api'

class ImageUploadForm extends Component {
  constructor(props) {
    super(props)
    this.state = {
      file: {},
      progress: 0,
      submitEnabled: false,
    }
  }

  updateProgress = progress => {
    this.setState({
      progress,
    })
  }

  render() {
    const {t} = this.props
    return (
      <Form
        onSubmit={async evt => {
          evt.preventDefault()
          const response = await uploadFile(
            this.props.apiContextPath,
            this.state.file,
            'fileResource',
            this.updateProgress
          )
          const responseJson = JSON.parse(response)
          const label = Object.values(responseJson.label)[0]
          this.props.onChange({
            label: label,
            title: label,
            url: responseJson.uri,
          })
          this.props.onSubmit()
        }}
      >
        <FileUploadForm
          filename={this.state.file.name}
          onChange={file =>
            this.setState({
              file: file,
              submitEnabled: true,
            })
          }
          progress={this.state.progress}
        />
        <Button
          className="float-right"
          color="primary"
          disabled={!this.state.submitEnabled}
          type="submit"
        >
          {t('next')}
        </Button>
      </Form>
    )
  }
}

export default withTranslation()(ImageUploadForm)
