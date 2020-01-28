import React from 'react'
import {
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  Label,
  Progress,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const FileUploadForm = props => {
  const {t} = useTranslation()
  const {filename, onChange, progress} = props
  return (
    <>
      <FormGroup>
        <Label className="font-weight-bold" for={'filename'}>
          {t('file')}
        </Label>
        <InputGroup>
          <Input
            className="rounded"
            id="filename"
            readOnly
            type="text"
            value={filename || ''}
          />
          <InputGroupAddon addonType="append" className="ml-1">
            <Label
              className="btn btn-info rounded"
              for={'file-upload'}
              id="file-upload-button"
            >
              {t('chooseFile')}
            </Label>
            <Input
              className="d-none"
              id="file-upload"
              onChange={evt => onChange(evt.target.files[0])}
              type="file"
            />
          </InputGroupAddon>
        </InputGroup>
      </FormGroup>
      <FormGroup>
        <Progress animated color="info" value={progress}>
          {progress > 0 && `${progress}%`}
        </Progress>
      </FormGroup>
    </>
  )
}

export default FileUploadForm
