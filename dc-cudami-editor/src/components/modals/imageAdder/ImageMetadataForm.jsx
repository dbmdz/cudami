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
  attributes,
  isOpen,
  onChange,
  toggle,
  toggleTooltip,
  tooltipsOpen,
}) => {
  const {t} = useTranslation()
  return (
    <Card>
      <CardHeader>
        <Button
          className="font-weight-bold ml-0 p-0"
          color="link"
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
                  <FaQuestionCircle
                    id="caption-tooltip"
                    style={{cursor: 'pointer'}}
                  />
                  <InputTooltip
                    isOpen={tooltipsOpen.caption}
                    target="caption-tooltip"
                    text={t('tooltips.caption')}
                    toggle={() => toggleTooltip('caption')}
                  />
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
                  <FaQuestionCircle
                    id="title-tooltip"
                    style={{cursor: 'pointer'}}
                  />
                  <InputTooltip
                    isOpen={tooltipsOpen.title}
                    target="title-tooltip"
                    text={t('tooltips.title')}
                    toggle={() => toggleTooltip('title')}
                  />
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
                required
                type="text"
                value={attributes.altText}
              />
              <InputGroupAddon addonType="append">
                <InputGroupText>
                  <FaQuestionCircle
                    id="altText-tooltip"
                    style={{cursor: 'pointer'}}
                  />
                  <InputTooltip
                    isOpen={tooltipsOpen.altText}
                    target="altText-tooltip"
                    text={t('tooltips.altText')}
                    toggle={() => toggleTooltip('altText')}
                  />
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
