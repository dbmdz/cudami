import React, { Component } from 'react';
import {
  Container,
  Label
} from 'reactstrap';

import './IdentifiableForm.css';
import LocaleAdderModal from './modals/LocaleAdderModal';
import WebpageForm from './WebpageForm';
import WebsiteForm from './WebsiteForm';
import {
  getAvailableLocales,
  loadAvailableLocales,
  loadIdentifiable,
  saveIdentifiable,
  updateIdentifiable
} from '../api';
import initI18n from '../i18n'
import IFrameAdderModal from './modals/IFrameAdderModal';

class IdentifiableForm extends Component {
  constructor(props){
    super(props);
    this.state = {
      activeLocale: props.activeLocale,
      availableLocales: [],
      identifiable: null,
      modalsOpen: {
        iframeAdder: false,
        localeAdder: false
      }
    };
  }

  async componentDidMount(){
    initI18n(this.props.uiLocale);
    const availableLocales = this.props.mockApi ? getAvailableLocales() : await loadAvailableLocales();
    const identifiable = await loadIdentifiable(
      this.props.type,
      this.props.uuid
    );
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
        />;*/
      case 'webpage':
        return <WebpageForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onAddLocale={this.toggleModal}
          onSave={this.sendIdentifiable}
          onToggleLocale={this.toggleLocale}
          onUpdate={this.updateIdentifiable}
        />;
      case 'website':
        return <WebsiteForm
          activeLocale={this.state.activeLocale}
          identifiable={this.state.identifiable}
          onAddLocale={this.toggleModal}
          onSave={this.sendIdentifiable}
          onToggleLocale={this.toggleLocale}
          onUpdate={this.updateIdentifiable}
        />;
      default:
        return <></>;
    }
  }

  sendIdentifiable =  () => {
    if (this.state.identifiable.uuid) {
      updateIdentifiable(this.state.identifiable, this.props.type, this.state.identifiable.uuid);
    } else {
      saveIdentifiable(this.state.identifiable, this.props.type);
    }
  };

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
            <>
              <Label className='font-weight-bold mt-3'>JSON (debug)</Label>
              <pre className='border'>
                <code>
                  {JSON.stringify(this.state.identifiable, null, 4)}
                </code>
              </pre>
            </>
          }
          <IFrameAdderModal
            isOpen={this.state.modalsOpen.iframeAdder}
            onToggle={() => this.toggleModal('iframeAdder')}
          />
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
  debug: false,
  mockApi: false
};

export default IdentifiableForm;
