import './FileUploadForm.css'

import {FileDrop} from 'react-file-drop'
import {useTranslation} from 'react-i18next'
import {FaCopy, FaUpload} from 'react-icons/fa'
import {Input, Label, Progress} from 'reactstrap'

const FileUploadForm = ({onChange, progress}) => {
  const {t} = useTranslation()
  return (
    <>
      <FileDrop
        onDrop={(files) => files && onChange(files[0])}
        draggingOverTargetClassName="file-drop-dragging-over-target"
        targetClassName="file-drop-target p-1"
      >
        <div className="mb-3">
          <FaUpload size="30" />
        </div>
        <div className="mb-3">{t('dragAndDropImage')}</div>
        <Label
          className="btn btn-primary m-0 rounded"
          for="file-upload"
          id="file-upload-button"
        >
          <FaCopy className="mr-1" />
          {t('chooseFile')}
        </Label>
        <Input
          className="d-none"
          id="file-upload"
          onChange={(evt) => {
            const {files} = evt.target
            if (files.length) {
              onChange(files[0])
            }
          }}
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
