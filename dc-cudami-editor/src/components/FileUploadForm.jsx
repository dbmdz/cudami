import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import {faCopy, faUpload} from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import {Input, Label, Progress} from 'reactstrap'
import FileDrop from 'react-file-drop'
import {useTranslation} from 'react-i18next'

import './FileUploadForm.css'

const FileUploadForm = props => {
  const {t} = useTranslation()
  const {onChange, progress} = props
  return (
    <>
      <FileDrop
        onDrop={files => onChange(files[0])}
        draggingOverTargetClassName="file-drop-dragging-over-target"
        targetClassName="file-drop-target p-1"
      >
        <div className="mb-3">
          <FontAwesomeIcon icon={faUpload} size="2x" />
        </div>
        <div className="mb-3">{t('dragAndDropImage')}</div>
        <Label
          className="btn btn-primary m-0 rounded"
          for="file-upload"
          id="file-upload-button"
        >
          <FontAwesomeIcon className="mr-1" icon={faCopy} />
          {t('chooseFile')}
        </Label>
        <Input
          className="d-none"
          id="file-upload"
          onChange={evt => onChange(evt.target.files[0])}
          type="file"
        />
      </FileDrop>
      {progress > 0 && progress < 100 && (
        <Progress animated className="mt-3" color="info" value={progress}>
          {`${progress}%`}
        </Progress>
      )}
    </>
  )
}

export default FileUploadForm
