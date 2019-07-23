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
import LocaleAdder from './LocaleAdder';
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
            {Object.entries(props.identifiable.label).map(([locale]) => <LocaleTab
              activeLocale={props.activeLocale}
              key={locale}
              locale={locale}
              onClick={(locale => props.onToggleLocale(locale))}
            />)}
            <LocaleAdder onClick={props.onAddLocale} />
          </Nav>
          <TabContent activeTab={props.activeLocale}>
            {Object.entries(props.identifiable.label).map(([locale, text]) => <LocaleTabContent
              description={props.identifiable.description[locale]}
              key={locale}
              label={text}
              locale={locale}
              onUpdate={(updateKey, updateValue) => props.onUpdate({
                ...props.identifiable,
                [updateKey]: {
                  ...props.identifiable[updateKey],
                  [locale]: updateValue
                }
              })}
            />)}
          </TabContent>
        </Col>
      </Row>
    </Form>
  )
};

export default WebsiteForm;
