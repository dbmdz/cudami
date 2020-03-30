import React, {Component} from 'react'
import {Button, ButtonGroup, Card, Col, Form, Input, Row} from 'reactstrap'
import {withTranslation} from 'react-i18next'

import FileUploadForm from './FileUploadForm'
import {uploadFile} from '../api'

class FileResourceUploadForm extends Component {
  constructor(props) {
    super(props)
    this.state = {
      file: {},
      progress: 0,
      submitEnabled: false,
    }
  }

  updateProgress = (progress) => {
    this.setState({
      progress,
    })
  }

  render() {
    const {t} = this.props
    return (
      <Form
        onSubmit={async (evt) => {
          evt.preventDefault()
          const responseJson = await uploadFile(
            this.props.apiContextPath,
            this.state.file,
            this.updateProgress
          )
          this.props.onUpdate(JSON.parse(responseJson))
        }}
      >
        <Row>
          <Col xs="6" sm="9">
            <h1>{t('uploadFileResource')}</h1>
          </Col>
          <Col xs="6" sm="3">
            <ButtonGroup className="float-right">
              <Button
                color="primary"
                disabled={!this.state.submitEnabled}
                type="submit"
              >
                {t('next')}
              </Button>
            </ButtonGroup>
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            <hr />
          </Col>
        </Row>
        <Row>
          <Col sm="12">
            <Card body className="text-center">
              <FileUploadForm
                onChange={(file) =>
                  this.setState({
                    file: file,
                    submitEnabled: true,
                  })
                }
                progress={this.state.progress}
              />
              <Input
                className="mt-3 rounded"
                id="filename"
                readOnly
                type="text"
                value={this.state.file.name || ''}
              />
            </Card>
          </Col>
        </Row>
      </Form>
    )
  }
}

export default withTranslation()(FileResourceUploadForm)
