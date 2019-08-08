import React, { Component } from 'react';
import {
  Container,
  Label
} from 'reactstrap';

import './IdentifiableForm.css';
import LanguageAdderModal from './modals/LanguageAdderModal';
import ContentNodeForm from './ContentNodeForm';
import ContentTreeForm from './ContentTreeForm';
import WebpageForm from './WebpageForm';
import WebsiteForm from './WebsiteForm';
import {
  getAvailableLanguages,
  loadAvailableLanguages,
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
      activeLanguage: props.activeLanguage,
      availableLanguages: [],
      identifiable: null,
      modalsOpen: {
        iframeAdder: false,
        languageAdder: false
      }
    };
  }

  async componentDidMount(){
    const i18n = initI18n(this.props.uiLocale);
    const availableLanguages = this.props.mockApi ? getAvailableLanguages() : await loadAvailableLanguages();
    let identifiable = await loadIdentifiable(
      this.props.type, this.props.uuid || 'new'
    );
    if (!identifiable.uuid) {
      identifiable = {
        ...identifiable,
        description: {},
        label: {
          [this.state.activeLanguage]: ''
        },
        text: ['article', 'webpage'].includes(this.props.type) ? {} : undefined
      }
    }
    this.setState({
      availableLanguages: availableLanguages.reduce((languages, language) => {
        if (!(language in identifiable.label)) {
          languages.push({
            displayName: i18n.t(`languageNames:${language}`),
            name: language
          });
        }
        return languages;
      }, []).sort((a, b) => (a.displayName > b.displayName) ? 1 : -1),
      identifiable
    });
  }

  addLanguage = (modalName) => {
    const selectedLanguage = this.state.selectedLanguage || this.state.availableLanguages[0].name;
    this.setState({
      activeLanguage: selectedLanguage,
      availableLanguages: this.state.availableLanguages.filter(
        language => language.name !== selectedLanguage
      ),
      identifiable: {
        ...this.state.identifiable,
        label: {
          ...this.state.identifiable.label,
          [selectedLanguage]: ''
        }
      },
      modalsOpen: {
        ...this.state.modalsOpen,
        [modalName]: !this.state.modalsOpen[modalName]
      },
      selectedLanguage: undefined
    });
  }

  getFormComponent(){
    switch (this.props.type) {
      /*case 'article':
        return <ArticleForm
          activeLanguage={this.state.activeLanguage}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;*/
      case 'contentNode':
        return <ContentNodeForm
          activeLanguage={this.state.activeLanguage}
          canAddLanguage={this.state.availableLanguages.length > 0}
          identifiable={this.state.identifiable}
          onAddLanguage={this.toggleModal}
          onSubmit={this.submitIdentifiable}
          onToggleLanguage={this.toggleLanguage}
          onUpdate={this.updateIdentifiable}
        />;
      case 'contentTree':
        return <ContentTreeForm
          activeLanguage={this.state.activeLanguage}
          canAddLanguage={this.state.availableLanguages.length > 0}
          identifiable={this.state.identifiable}
          onAddLanguage={this.toggleModal}
          onSubmit={this.submitIdentifiable}
          onToggleLanguage={this.toggleLanguage}
          onUpdate={this.updateIdentifiable}
        />;
      /*case 'fileResource':
        return <FileResourceForm
          activeLanguage={this.state.activeLanguage}
          identifiable={this.state.identifiable}
          onUpdate={this.updateIdentifiable}
        />;*/
      case 'webpage':
        return <WebpageForm
          activeLanguage={this.state.activeLanguage}
          canAddLanguage={this.state.availableLanguages.length > 0}
          identifiable={this.state.identifiable}
          onAddLanguage={this.toggleModal}
          onSubmit={this.submitIdentifiable}
          onToggleLanguage={this.toggleLanguage}
          onUpdate={this.updateIdentifiable}
        />;
      case 'website':
        return <WebsiteForm
          activeLanguage={this.state.activeLanguage}
          canAddLanguage={this.state.availableLanguages.length > 0}
          identifiable={this.state.identifiable}
          onAddLanguage={this.toggleModal}
          onSubmit={this.submitIdentifiable}
          onToggleLanguage={this.toggleLanguage}
          onUpdate={this.updateIdentifiable}
        />;
      default:
        return <></>;
    }
  }

  setSelectedLanguage = (selectedLanguage) => {
    this.setState({
      selectedLanguage
    });
  };

  submitIdentifiable = () => {
    if (this.props.uuid) {
      updateIdentifiable(this.state.identifiable, this.props.type, this.props.uuid);
    } else {
      saveIdentifiable(this.state.identifiable, this.props.type);
    }
  };

  toggleLanguage = (activeLanguage) => {
    this.setState({
      activeLanguage
    })
  };

  toggleModal = (name) => {
    this.setState({
      modalsOpen: {
        ...this.state.modalsOpen,
        [name]: !this.state.modalsOpen[name]
      },
      selectedLanguage: undefined
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
          <LanguageAdderModal
            availableLanguages={this.state.availableLanguages}
            isOpen={this.state.modalsOpen.languageAdder}
            onSelect={this.setSelectedLanguage}
            onSubmit={() => this.addLanguage('languageAdder')}
            onToggle={() => this.toggleModal('languageAdder')}
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
