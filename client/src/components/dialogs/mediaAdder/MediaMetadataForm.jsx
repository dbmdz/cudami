import {useTranslation} from 'react-i18next'
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

import InfoTooltip from '../../InfoTooltip'
import InputWithFloatingLabel from '../../InputWithFloatingLabel'

const MediaMetadataForm = ({
  altText,
  caption,
  enableAltText = true,
  enableCaption = true,
  enableTitle = true,
  isOpen,
  mediaType,
  onChange,
  title,
  toggle,
}) => {
  const {t} = useTranslation()
  return (
    <Card className="media-adder-content media-metadata">
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
                <InputWithFloatingLabel
                  label={t('caption')}
                  name={`${mediaType}-caption`}
                  onChange={(value) => onChange('caption', value)}
                  value={caption}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    name={`${mediaType}-caption`}
                    text={t('tooltips.caption')}
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
          )}
          {enableTitle && (
            <FormGroup>
              <InputGroup>
                <InputWithFloatingLabel
                  label={t('tooltip')}
                  name={`${mediaType}-title`}
                  onChange={(value) => onChange('title', value)}
                  value={title}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    name={`${mediaType}-title`}
                    text={t('tooltips.title')}
                  />
                </InputGroupAddon>
              </InputGroup>
            </FormGroup>
          )}
          {enableAltText && (
            <FormGroup>
              <InputGroup>
                <InputWithFloatingLabel
                  label={t('altText')}
                  name={`${mediaType}-alttext`}
                  onChange={(value) => onChange('altText', value)}
                  value={altText}
                />
                <InputGroupAddon addonType="append">
                  <InfoTooltip
                    name={`${mediaType}-alttext`}
                    text={t('tooltips.altText')}
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

export default MediaMetadataForm
