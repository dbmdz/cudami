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
  Popover,
  PopoverBody,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

const InputTooltip = ({isOpen, target, text, toggle}) => {
  return (
    <Popover isOpen={isOpen} placement="left" target={target} toggle={toggle}>
      <PopoverBody>{text}</PopoverBody>
    </Popover>
  )
}

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
                <Button
                  className="border"
                  color="light"
                  id="caption-tooltip"
                  type="button"
                >
                  <FaQuestionCircle />
                </Button>
                <InputTooltip
                  isOpen={tooltipsOpen.caption}
                  target="caption-tooltip"
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
                <Button
                  className="border"
                  color="light"
                  id="title-tooltip"
                  type="button"
                >
                  <FaQuestionCircle />
                </Button>
                <InputTooltip
                  isOpen={tooltipsOpen.title}
                  target="title-tooltip"
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
                <Button
                  className="border"
                  color="light"
                  id="alttext-tooltip"
                  type="button"
                >
                  <FaQuestionCircle />
                </Button>
                <InputTooltip
                  isOpen={tooltipsOpen.altText}
                  target="alttext-tooltip"
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
