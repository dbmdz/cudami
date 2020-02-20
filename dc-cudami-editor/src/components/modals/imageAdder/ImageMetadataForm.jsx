import React from 'react'
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Collapse,
  FormGroup,
  Input,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const ImageMetadataForm = props => {
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
            <Input
              name="caption"
              onChange={evt => onChange('caption', evt.target.value)}
              placeholder={t('caption')}
              type="text"
              value={attributes.caption}
            />
          </FormGroup>
          <FormGroup>
            <Input
              name="title"
              onChange={evt => onChange('title', evt.target.value)}
              placeholder={t('title')}
              type="text"
              value={attributes.title}
            />
          </FormGroup>
          <FormGroup className="mb-0">
            <Input
              name="altText"
              onChange={evt => onChange('altText', evt.target.value)}
              placeholder={t('altText')}
              type="text"
              value={attributes.altText}
            />
          </FormGroup>
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default ImageMetadataForm
