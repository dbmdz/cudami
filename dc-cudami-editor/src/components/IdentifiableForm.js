import React, { Component } from 'react';
import {
  Container
} from 'reactstrap';

import WebsiteForm from './WebsiteForm';
import { loadIdentifiable } from '../Api';

class IdentifiableForm extends Component {
  constructor(props){
    super(props);
    this.state = {
      activeLocale: props.activeLocale,
      identifiable: null
    };
  }

  async componentDidMount(){
    const identifiable = await loadIdentifiable(
      this.props.type, this.props.uuid
    );
    this.setState({
      identifiable
    });
  }

  getFormComponent(){
    switch (this.props.type) {
      /*case 'article':
        return <ArticleForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;
      case 'contentNode':
        return <ContentNodeForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;
      case 'contentTree':
        return <ContentTreeForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;
      case 'fileResource':
        return <FileResourceForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;
      case 'webpage':
        return <WebpageForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;*/
      case 'website':
        return <WebsiteForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onToggleLocale={this.toggleLocale}
          onUpdate={this.updateIdentifiable}
        />;
      default:
        return <></>;
    }
  }

  toggleLocale = (activeLocale) => {
    this.setState({
      activeLocale
    })
  };

  updateIdentifiable = (identifiable) => {
    this.setState({
      identifiable
    });
  };

  render(){
    return this.state.identifiable
      ? <Container>
        {this.getFormComponent()}
        {
          this.props.debug &&
          <pre className='mt-3'>
            <code>
              {JSON.stringify(this.state.identifiable, null, 4)}
            </code>
          </pre>
        }
      </Container>
      : <></>;
  }
}

IdentifiableForm.defaultProps = {
  debug: false
};

export default IdentifiableForm;
