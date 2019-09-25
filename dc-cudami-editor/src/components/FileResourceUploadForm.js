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
  Progress,
  Row
} from 'reactstrap';
import { withTranslation } from 'react-i18next';

import { uploadFile } from '../api';

class FileResourceUploadForm extends Component {
  constructor(props){
    super(props);
    this.state = {
      file: {},
      progress: 0,
      submitEnabled: false
    };
  }

  updateProgress = (progress) => {
    this.setState({
      progress
    });
  }

  render(){
    const { t } = this.props;
    return (
      <Form onSubmit={async (evt) => {
        evt.preventDefault();
        const responseJson = await uploadFile(
          this.props.apiContextPath,
          this.state.file,
          this.props.type,
          this.updateProgress
        );
        this.props.onUpdate(JSON.parse(responseJson));
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
      </Form>
    );
  }
}

export default withTranslation()(FileResourceUploadForm);
