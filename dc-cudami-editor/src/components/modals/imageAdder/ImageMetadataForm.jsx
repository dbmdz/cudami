import React, {useState} from 'react'
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

const InputTooltip = (props) => {
  const {target, text} = props
  const [tooltipOpen, setTooltipOpen] = useState(false)
  const toggle = () => setTooltipOpen(!tooltipOpen)
  return (
    <Popover
      isOpen={tooltipOpen}
      placement="left"
      target={target}
      toggle={toggle}
    >
      <PopoverBody>{text}</PopoverBody>
    </Popover>
  )
}

const ImageMetadataForm = (props) => {
  const {t} = useTranslation()
  const {attributes, isOpen, onChange, toggle} = props
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
                    target="caption-tooltip"
                    text={t('tooltips.caption')}
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
                    target="title-tooltip"
                    text={t('tooltips.title')}
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
                    target="altText-tooltip"
                    text={t('tooltips.altText')}
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
