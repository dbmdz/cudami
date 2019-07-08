import React, { PureComponent } from 'react';
import {
  Col,
  Container,
  Form,
  Row
} from 'reactstrap';

import FormButtonsComponent from './FormButtonsComponent';
import FormIdComponent from './FormIdComponent';
import FormTitleComponent from './FormTitleComponent';
import FormUrlComponent from './FormUrlComponent';
import IdentifiableEditorComponent from './IdentifiableEditorComponent';
import IdentifiableController from '../controllers/IdentifiableController';

class IdentifiableComponent extends PureComponent {
  identifiableController = new IdentifiableController();

  state = {
    activeLocale: 'de_DE',
    identifiable: {}
  };

  async componentDidMount(){
    const identifiable = await this.identifiableController.loadData();
    this.setState({
      ...this.state,
      identifiable
    });
  }

  updateDescription(description) {
    let identifiable = {...this.state.identifiable};
    identifiable.description.localizedStructuredContent[this.state.activeLocale] = description;
    this.setState({...this.state, identifiable});
  }

  updateLabel(label) {
    let identifiable = {...this.state.identifiable};
    identifiable.label.translations.map(translation => {
      if (translation.locale === this.state.activeLocale) {
        translation.text = label;
      }
      return translation;
    });
    this.setState({...this.state, identifiable});
  }

  updateUrl(url) {
    let identifiable = {...this.state.identifiable};
    identifiable.url = url;
    this.setState({...this.state, identifiable});
  }

  render(){
    if (Object.entries(this.state.identifiable).length > 0) {
      return (
        <Container fluid={true}>
          <Form>
            <Row>
              <Col xs='6' sm='6'>
                <FormTitleComponent url={this.state.identifiable.url} />
              </Col>
              <Col xs='6' sm='6'>
                <FormButtonsComponent />
              </Col>
            </Row>
            <Row>
              <Col sm='12'>
                <hr />
              </Col>
            </Row>
            <Row>
              <Col sm='12'>
                <FormIdComponent id={this.state.identifiable.uuid} />
                {this.state.identifiable.url && <FormUrlComponent
                  onChange={evt => this.updateUrl(evt.target.value)}
                  url={this.state.identifiable.url}
                />}
                <IdentifiableEditorComponent
                  activeLocale={this.state.activeLocale}
                  description={this.state.identifiable.description}
                  label={this.state.identifiable.label}
                  text={this.state.identifiable.text}
                  updateDescription={doc => this.updateDescription(doc)}
                  updateLabel={evt => this.updateLabel(evt.target.value)}
                  updateLocale={locale => {this.state.activeLocale !== locale && this.setState({...this.state, activeLocale: locale})}}
                />
              </Col>
            </Row>
          </Form>
        </Container>
      );
    }
    return (<></>);
  }
};

export default IdentifiableComponent;
