import React from 'react'
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Collapse,
  FormGroup,
  InputGroup,
  InputGroupAddon,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import FloatingLabelInput from '../../FloatingLabelInput'
import InfoTooltip from '../../InfoTooltip'

const ImageMetadataForm = ({
  altText,
  caption,
  enableAltText,
  enableCaption,
  enableTitle,
  isOpen,
  onChange,
  title,
  toggle,
  toggleTooltip,
  tooltipsOpen,
}) => {
  const {t} = useTranslation()
  return (
    <Card className="image-metadata mb-2">
      <CardHeader>
        <Button className="font-weight-bold p-0" color="link" onClick={toggle}>
          {t('enterMetadata')}
        </Button>
      </CardHeader>
      <Collapse isOpen={isOpen}>
        <CardBody>
          {enableCaption && (
            <FormGroup>
              <InputGroup>
                <FloatingLabelInput
                  label={t('caption')}
                  name="image-caption"
                  onChange={(value) => onChange('caption', value)}
                  value={caption}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    isOpen={tooltipsOpen.caption}
                    name="image-caption"
                    text={t('tooltips.caption')}
                    toggle={() => toggleTooltip('caption')}
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
          )}
          {enableTitle && (
            <FormGroup>
              <InputGroup>
                <FloatingLabelInput
                  label={t('tooltip')}
                  name="image-title"
                  onChange={(value) => onChange('title', value)}
                  value={title}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    isOpen={tooltipsOpen.title}
                    name="image-title"
                    text={t('tooltips.title')}
                    toggle={() => toggleTooltip('title')}
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
          )}
          {enableAltText && (
            <FormGroup>
              <InputGroup>
                <FloatingLabelInput
                  label={t('altText')}
                  name="image-alttext"
                  onChange={(value) => onChange('altText', value)}
                  value={altText}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    isOpen={tooltipsOpen.altText}
                    name="image-alttext"
                    text={t('tooltips.altText')}
                    toggle={() => toggleTooltip('altText')}
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
          )}
        </CardBody>
      </Collapse>
    </Card>
  )
}

ImageMetadataForm.defaultProps = {
  enableAltText: true,
  enableCaption: true,
  enableTitle: true,
}

export default ImageMetadataForm
