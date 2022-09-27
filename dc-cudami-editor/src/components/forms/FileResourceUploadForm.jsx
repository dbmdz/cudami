import {useContext, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {Button, Card, Col, Form, Input, Row} from 'reactstrap'

import {uploadFile} from '../../api'
import AppContext from '../AppContext'
import FileUploadForm from '../FileUploadForm'
import Header from './Header'

const FileResourceUploadForm = ({formId, onUpdate}) => {
  const {apiContextPath} = useContext(AppContext)
  const [file, setFile] = useState()
  const [progress, setProgress] = useState(0)
  const [submitEnabled, setSubmitEnabled] = useState(false)
  const {t} = useTranslation()
  return (
    <Form
      onSubmit={async (evt) => {
        evt.preventDefault()
        const response = await uploadFile(apiContextPath, file, setProgress)
        onUpdate(response)
      }}
    >
      <Header
        buttonsDisabled={!submitEnabled}
        formId={formId}
        heading={t('uploadFileResource')}
      />
      <Row>
        <Col sm="12">
          <Card body className="text-center">
            <FileUploadForm
              onChange={(file) => {
                setFile(file)
                setSubmitEnabled(true)
              }}
              progress={progress}
            />
            <Input
              className="mt-3 rounded"
              id="filename"
              readOnly
              type="text"
              value={file?.name ?? ''}
            />
          </Card>
        </Col>
      </Row>
    </Form>
  )
}

export default FileResourceUploadForm
