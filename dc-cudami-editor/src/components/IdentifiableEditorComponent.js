import React from 'react';
import {
  Card,
  CardBody,
  Nav,
  NavItem,
  NavLink,
  TabContent,
  TabPane
} from 'reactstrap';
import FormLabelComponent from './FormLabelComponent';
import FormEditorComponent from './FormEditorComponent';

const IdentifiableEditorComponent = (props) => {
  return (
    <>
      <Nav tabs>
        {props.label.translations.map((translation, index) => {
          return (
            <NavItem key={index}>
              <NavLink
                className={props.activeLocale === translation.locale ? 'active' : ''}
                onClick={() => props.updateLocale(translation.locale)}
              >
                {translation.locale}
              </NavLink>
            </NavItem>
          );
        })}
      </Nav>
      <Card>
        <CardBody className='bg-light'>          
          <TabContent activeTab={props.activeLocale}>
            {props.label.translations.map((translation, index) => {
              return (
                <TabPane key={index} tabId={translation.locale}>
                  <FormLabelComponent
                    label={translation.text}
                    locale={translation.locale}
                    updateLabel={props.updateLabel}
                  />
                  <FormEditorComponent
                    description={props.description.localizedStructuredContent[translation.locale]}
                    locale={translation.locale}
                    updateDocument={props.updateDescription}
                  />
                  <div id={'output-' + translation.locale}></div>
                </TabPane>
              );
            })}
          </TabContent>
        </CardBody>
      </Card>
    </>
  );
}

export default IdentifiableEditorComponent;
