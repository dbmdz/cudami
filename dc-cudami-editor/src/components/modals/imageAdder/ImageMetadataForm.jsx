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
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import InfoTooltip from '../../InfoTooltip'

const ImageMetadataForm = ({
  altText,
  caption,
  isOpen,
  onChange,
  title,
  toggle,
  toggleTooltip,
  tooltipsOpen,
}) => {
  const {t} = useTranslation()
  return (
    <Card>
      <CardHeader>
        <Button className="font-weight-bold p-0" color="link" onClick={toggle}>
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
                type="text"
                value={caption}
              />
              <span className="floating-label">{t('caption')}</span>
              <InputGroupAddon addonType="append">
                <InfoTooltip
                  isOpen={tooltipsOpen.caption}
                  name="caption"
                  text={t('tooltips.caption')}
                  toggle={() => toggleTooltip('caption')}
                />
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
          <FormGroup>
            <InputGroup>
              <Input
                name="title"
                onChange={(evt) => onChange('title', evt.target.value)}
                type="text"
                value={title}
              />
              <span className="floating-label">{t('tooltip')}</span>
              <InputGroupAddon addonType="append">
                <InfoTooltip
                  isOpen={tooltipsOpen.title}
                  name="title"
                  text={t('tooltips.title')}
                  toggle={() => toggleTooltip('title')}
                />
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
          <FormGroup className="mb-0">
            <InputGroup>
              <Input
                name="altText"
                onChange={(evt) => onChange('altText', evt.target.value)}
                type="text"
                value={altText}
              />
              <span className="floating-label">{t('altText')}</span>
              <InputGroupAddon addonType="append">
                <InfoTooltip
                  isOpen={tooltipsOpen.altText}
                  name="alttext"
                  text={t('tooltips.altText')}
                  toggle={() => toggleTooltip('altText')}
                />
              </InputGroupAddon>
            </InputGroup>
          </FormGroup>
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default ImageMetadataForm
