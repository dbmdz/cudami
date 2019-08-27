import React, { Component } from 'react';
import {
  Button,
  ButtonGroup,
  Col,
  Form,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  Label,
  Nav,
  Progress,
  Row,
  TabContent
} from 'reactstrap';
import { withTranslation } from 'react-i18next';

import './FileResourceForm.css';
import FormIdInput from './FormIdInput';
import FormButtons from './FormButtons';
import LanguageAdder from './LanguageAdder';
import LanguageTab from './LanguageTab';
import LanguageTabContent from './LanguageTabContent';

class FileResourceForm extends Component {
  constructor(props){
    super(props);
    this.state = {
      file: {},
      progress: 0,
      submitEnabled: false
    };
  }

  getFileUploadForm = (t) => {
    return <Form onSubmit={evt => {
        evt.preventDefault();
        this.uploadFile(
          this.state.file,
          this.props.type
        );
      }}>
        <Row>
          <Col xs='6' sm='9'>
            <h1>
              {t('uploadFileResource')}
            </h1>
          </Col>
          <Col xs='6' sm='3'>
            <ButtonGroup className='float-right'>
              <Button
                className='mr-1'
                color='secondary'
                type='button'
              >
                {t('abort')}
              </Button>
              <Button
                color='primary'
                disabled={!this.state.submitEnabled && true}
                type='submit'
              >
                {t('next')}
              </Button>
            </ButtonGroup>
          </Col>
        </Row>
        <Row>
          <Col sm='12'>
            <hr />
          </Col>
        </Row>
        <Row>
          <Col sm='12'>
            <FormGroup>
              <Label className='font-weight-bold' for={'filename'}>{t('file')}</Label>
              <InputGroup>
                <Input
                  id='filename'
                  readOnly='readonly'
                  type='text'
                  value={this.state.file.name || ''}
                />
                <InputGroupAddon addonType='append' className='ml-1'>
                  <Label className='btn btn-info' for={'file-upload'} id='file-upload-button'>
                    {t('chooseFile')}
                  </Label>
                  <Input
                    className='d-none'
                    id='file-upload'
                    onChange={evt => this.setState({file: evt.target.files[0], submitEnabled: true})}
                    type='file'
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
            <Progress animated color="info" value={this.state.progress}>
              {this.state.progress > 0 && `${this.state.progress}%`}
            </Progress>
          </Col>
        </Row>
      </Form>;
  };

  getMetadataForm = (t) => {
    return <Form onSubmit={evt => {
        evt.preventDefault();
        this.props.onSubmit();
      }}>
        <Row>
          <Col xs='6' sm='9'>
            <h1>
              {t('editFileResource', {name: this.props.identifiable.filename})}
            </h1>
          </Col>
          <Col xs='6' sm='3'>
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
            <FormIdInput id={this.props.identifiable.uuid} />
            <Nav tabs>
              {Object.entries(this.props.identifiable.label).map(([language]) => <LanguageTab
                activeLanguage={this.props.activeLanguage}
                key={language}
                language={language}
                onClick={(language => this.props.onToggleLanguage(language))}
              />)}
              {this.props.canAddLanguage && <LanguageAdder onClick={this.props.onAddLanguage} />}
            </Nav>
            <TabContent activeTab={this.props.activeLanguage}>
              {Object.entries(this.props.identifiable.label).map(([language, text]) => <LanguageTabContent
                description={this.props.identifiable.description[language]}
                key={language}
                label={text}
                language={language}
                onUpdate={(updateKey, updateValue) => this.props.onUpdate({
                  ...this.props.identifiable,
                  [updateKey]: {
                    ...this.props.identifiable[updateKey],
                    [language]: updateValue
                  }
                })}
              />)}
            </TabContent>
          </Col>
        </Row>
      </Form>;
  };

  uploadFile = (file, type) => {
    const request = new XMLHttpRequest();
    request.onload = () => {
      this.props.onUpdate(JSON.parse(request.response));
    }
    request.upload.addEventListener('progress', evt => {
      if (evt.lengthComputable) {
        this.setState({
          progress: Math.round((evt.loaded / evt.total) * 100)
        });
      }
    });
    request.open('POST', `/api/${type.toLowerCase()}s/new/upload`, true);
    const formData = new FormData();
    formData.append('userfile', file, file.name);
    request.send(formData);
  };

  render(){
    const { t } = this.props;
    if (this.props.identifiable.uuid) {
      return <>{this.getMetadataForm(t)}</>;
    }
    return <>{this.getFileUploadForm(t)}</>;
  }
}

export default withTranslation()(FileResourceForm);
