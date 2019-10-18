import React from 'react'
import {Card, CardBody, TabPane} from 'reactstrap'

import FormEditor from './FormEditor'
import FormLabelInput from './FormLabelInput'

const LanguageTabContent = props => {
  return (
    <TabPane tabId={props.language}>
      <Card>
        <CardBody className="bg-light">
          <FormLabelInput
            label={props.label}
            language={props.language}
            onUpdate={evt => {
              props.onUpdate('label', evt.target.value)
            }}
          />
          <FormEditor
            document={props.description}
            type="description"
            onUpdate={document => {
              props.onUpdate('description', document)
            }}
          />
        </CardBody>
        {props.children && <CardBody>{props.children}</CardBody>}
      </Card>
    </TabPane>
  )
}

export default LanguageTabContent
