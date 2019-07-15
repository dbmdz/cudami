import React from 'react';
import {
  Col,
  Form,
  Nav,
  Row,
  TabContent
} from 'reactstrap';

import FormIdInput from './FormIdInput';
import FormUrlInput from './FormUrlInput';
import FormButtons from './FormButtons';
import LocaleTab from './LocaleTab';
import LocaleTabContent from './LocaleTabContent';

const WebsiteForm = (props) => {
  return (
    <Form>
      <Row>
        <Col xs='6' sm='6'>
          <h1>
            Website <a href={props.identifiable.url}>{props.identifiable.url}</a> bearbeiten
          </h1>
        </Col>
        <Col xs='6' sm='6'>
          <FormButtons />
        </Col>
      </Row>
      <Row>
        <Col sm='12'>
          <hr />
        </Col>
      </Row>
      <Row>
        <Col sm='12'>
          <FormIdInput id={props.identifiable.uuid} />
          <FormUrlInput
            onChange={evt => props.onUpdate({...props.identifiable, url: evt.target.value})}
            url={props.identifiable.url}
          />
          <Nav tabs>
            {props.identifiable.label.translations.map((translation, index) => <LocaleTab
              activeLocale={props.activeLocale}
              key={index}
              locale={translation.locale}
              onClick={(locale => props.onToggleLocale(locale))}
            />)}
          </Nav>
          <TabContent activeTab={props.activeLocale}>
            {props.identifiable.label.translations.map((translation, index) => <LocaleTabContent
              description={props.identifiable.description[translation.locale]}
              key={index}
              locale={translation.locale}
              onUpdate={(updateKey, updateValue) => props.onUpdate({...props.identifiable, [updateKey]: {...props.identifiable.description, [translation.locale]: updateValue}})}
              text={translation.text}
            />)}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
};

export default WebsiteForm;
