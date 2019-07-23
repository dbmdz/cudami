import React, { Component } from 'react';
import {
  Container
} from 'reactstrap';

import './IdentifiableForm.css';
import LocaleAdderModal from './modals/LocaleAdderModal';
import WebsiteForm from './WebsiteForm';
import {
  loadAvailableLocales,
  loadIdentifiable
} from '../Api';

class IdentifiableForm extends Component {
  constructor (props) {
    super(props);
    this.state = {
      activeLocale: props.activeLocale,
      availableLocales: [],
      identifiable: null,
      modalsOpen: {
        localeAdder: false
      }
    };
  }

  async componentDidMount(){
    const identifiable = await loadIdentifiable(
      this.props.type, this.props.uuid
    );
    const availableLocales = await loadAvailableLocales();
    this.setState({
      availableLocales,
      identifiable
    });
  }

  addLocale = (modalName) => {
    const selectedLocale = this.state.selectedLocale || this.state.availableLocales[0];
    this.setState({
      activeLocale: selectedLocale,
      availableLocales: this.state.availableLocales.filter(
        locale => locale !== selectedLocale
      ),
      identifiable: {
        ...this.state.identifiable,
        label: {
          ...this.state.identifiable.label,
          [selectedLocale]: ''
        }
      },
      modalsOpen: {
        ...this.state.modalsOpen,
        [modalName]: !this.state.modalsOpen[modalName]
      },
      selectedLocale: undefined
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
          onAddLocale={this.toggleModal}
          onToggleLocale={this.toggleLocale}
          onUpdate={this.updateIdentifiable}
        />;
      default:
        return <></>;
    }
  }

  setSelectedLocale = (selectedLocale) => {
    this.setState({
      selectedLocale
    });
  };

  toggleLocale = (activeLocale) => {
    this.setState({
      activeLocale
    })
  };

  toggleModal = (name) => {
    this.setState({
      modalsOpen: {
        ...this.state.modalsOpen,
        [name]: !this.state.modalsOpen[name]
      },
      selectedLocale: undefined
    });
  };

  updateIdentifiable = (identifiable) => {
    this.setState({
      identifiable
    });
  };

  render(){
    return this.state.identifiable
      ? <Container id='cudami-editor'>
          {this.getFormComponent()}
          {
            this.props.debug &&
            <pre className='mt-3'>
              <code>
                {JSON.stringify(this.state.identifiable, null, 4)}
              </code>
            </pre>
          }
          <LocaleAdderModal
            availableLocales={this.state.availableLocales}
            isOpen={this.state.modalsOpen.localeAdder}
            onSelect={this.setSelectedLocale}
            onSubmit={() => this.addLocale('localeAdder')}
            onToggle={() => this.toggleModal('localeAdder')}
          />
        </Container>
      : <></>;
  }
}

IdentifiableForm.defaultProps = {
  debug: false
};

export default IdentifiableForm;
