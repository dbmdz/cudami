import React from 'react';
import {
  Col,
  Form,
  Nav,
  Row,
  TabContent
} from 'reactstrap';
import { useTranslation } from 'react-i18next';

import FormIdInput from './FormIdInput';
import FormButtons from './FormButtons';
import LocaleAdder from './LocaleAdder';
import LocaleTab from './LocaleTab';
import LocaleTabContent from './LocaleTabContent';

const ContentNodeForm = (props) => {
  const { t } = useTranslation();
  return (
    <Form onSubmit={(evt) => evt.preventDefault()}>
      <Row>
        <Col xs='6' sm='9'>
          <h1>
            {t('editContentTree')}
          </h1>
        </Col>
        <Col xs='6' sm='3'>
          <FormButtons onSave={props.onSave} />
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
          <Nav tabs>
            {Object.entries(props.identifiable.label).map(([locale]) => <LocaleTab
              activeLocale={props.activeLocale}
              key={locale}
              locale={locale}
              onClick={(locale => props.onToggleLocale(locale))}
            />)}
            {props.canAddLocale && <LocaleAdder onClick={props.onAddLocale} />}
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

export default ContentNodeForm;
