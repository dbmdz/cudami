import React from 'react'
import {Card, CardBody} from 'reactstrap'

import FormEditor from './FormEditor'
import FormLabelInput from './FormLabelInput'

const Teaser = ({description, label, language, onUpdate}) => {
  return (
    <Card className="bg-light">
      <CardBody>
        <FormLabelInput
          label={label}
          language={language}
          onUpdate={(label) => {
            onUpdate('label', label)
          }}
        />
        <FormEditor
          document={description}
          onUpdate={(document) => {
            onUpdate('description', document)
          }}
          restrictedMenu={true}
          type="description"
        />
      </CardBody>
    </Card>
  )
}

export default Teaser
