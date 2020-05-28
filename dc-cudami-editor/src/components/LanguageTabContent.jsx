import React from 'react'
import {Card, CardBody, TabPane} from 'reactstrap'

import FormEditor from './FormEditor'
import FormLabelInput from './FormLabelInput'

const LanguageTabContent = ({
  children,
  description,
  label,
  language,
  onUpdate,
}) => {
  return (
    <TabPane tabId={language}>
      <Card>
        <CardBody className="bg-light">
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
        {children && <CardBody>{children}</CardBody>}
      </Card>
    </TabPane>
  )
}

export default LanguageTabContent
