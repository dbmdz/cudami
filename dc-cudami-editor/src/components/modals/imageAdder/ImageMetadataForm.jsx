import React from 'react'
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Collapse,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

const ImageMetadataForm = (props) => {
  const {t} = useTranslation()
  const {attributes, isOpen, onChange, toggle, toggleEnabled} = props
  return (
    <Card>
      <CardHeader>
        <Button
          className="ml-0 p-0"
          color="link"
          disabled={!toggleEnabled}
          onClick={toggle}
        >
          {t('enterMetadata')}
        </Button>
      </CardHeader>
      <Collapse isOpen={isOpen}>
        <CardBody>
          <FormGroup>
            <InputGroup>
              <Input
                name="caption"
                onChange={(evt) => onChange('caption', evt.target.value)}
                placeholder={t('caption')}
                type="text"
                value={attributes.caption}
              />
              <InputGroupAddon addonType="append">
                <InputGroupText>
                  <FaQuestionCircle title={t('tooltips.caption')} />
                </InputGroupText>
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
          <FormGroup>
            <InputGroup>
              <Input
                name="title"
                onChange={(evt) => onChange('title', evt.target.value)}
                placeholder={t('tooltip')}
                type="text"
                value={attributes.title}
              />
              <InputGroupAddon addonType="append">
                <InputGroupText>
                  <FaQuestionCircle title={t('tooltips.tooltip')} />
                </InputGroupText>
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
          <FormGroup className="mb-0">
            <InputGroup>
              <Input
                name="altText"
                onChange={(evt) => onChange('altText', evt.target.value)}
                placeholder={t('altText')}
                type="text"
                value={attributes.altText}
              />
              <InputGroupAddon addonType="append">
                <InputGroupText>
                  <FaQuestionCircle title={t('tooltips.altText')} />
                </InputGroupText>
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default ImageMetadataForm
