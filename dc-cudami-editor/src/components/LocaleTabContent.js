import React from 'react';
import {
  Card,
  CardBody,
  TabPane
} from 'reactstrap';

import FormEditor from './FormEditor';
import FormLabelInput from './FormLabelInput';

const LocaleTabContent = (props) => {
  return (
    <TabPane tabId={props.locale}>
      <Card>
        <CardBody className='bg-light'>
          <FormLabelInput
            label={props.text}
            locale={props.locale}
          />
          <FormEditor
            document={props.description}
            type='description'
            onUpdate={document => {
              props.onUpdate('description', document)
            }}
          />
        </CardBody>
        {props.children && <CardBody>
          {props.children}
        </CardBody>}
      </Card>
    </TabPane>
  );
}

export default LocaleTabContent;
