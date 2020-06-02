import React from 'react'
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Collapse,
  CustomInput,
  FormGroup,
  Input,
  Label,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const ImageRenderingHintsForm = ({attributes, isOpen, onChange, toggle}) => {
  const {t} = useTranslation()
  return (
    <Card>
      <CardHeader>
        <Button
          className="font-weight-bold ml-0 p-0"
          color="link"
          onClick={toggle}
        >
          {t('defineRenderingHints')}
        </Button>
      </CardHeader>
      <Collapse isOpen={isOpen}>
        <CardBody>
          <FormGroup>
            <Label className="font-weight-bold" for="rendering-hints-alignment">
              {t('alignment')}
            </Label>
            <div id="rendering-hints-alignment">
              <CustomInput
                checked={attributes.alignment === 'left'}
                id="rendering-hits-alignment-left"
                inline
                label={t('left')}
                onChange={(evt) => onChange('alignment', evt.target.value)}
                type="radio"
                value="left"
              />
              <CustomInput
                checked={attributes.alignment === 'right'}
                id="rendering-hits-alignment-right"
                inline
                label={t('right')}
                onChange={(evt) => onChange('alignment', evt.target.value)}
                type="radio"
                value="right"
              />
            </div>
          </FormGroup>
          <FormGroup>
            <Label className="font-weight-bold" for="rendering-hints-width">
              {t('width')}
            </Label>
            <div id="rendering-hints-width">
              <CustomInput
                checked={attributes.width === '25%'}
                id="rendering-hits-width-25"
                inline
                label="25%"
                onChange={(evt) => onChange('width', evt.target.value)}
                type="radio"
                value="25%"
              />
              <CustomInput
                checked={attributes.width === '33%'}
                id="rendering-hits-width-33"
                inline
                label="33%"
                onChange={(evt) => onChange('width', evt.target.value)}
                type="radio"
                value="33%"
              />
              <CustomInput
                checked={attributes.width === '50%'}
                id="rendering-hits-width-50"
                inline
                label="50%"
                onChange={(evt) => onChange('width', evt.target.value)}
                type="radio"
                value="50%"
              />
              <CustomInput
                checked={attributes.width === '100%'}
                id="rendering-hits-width-100"
                inline
                label="100%"
                onChange={(evt) => onChange('width', evt.target.value)}
                type="radio"
                value="100%"
              />
            </div>
          </FormGroup>
          <FormGroup className="mb-0">
            <Label
              className="font-weight-bold"
              for="rendering-hints-image-link"
            >
              {t('imageLink')}
            </Label>
            <Input
              id="rendering-hints-image-link"
              name="imageLink"
              onChange={(evt) => onChange('linkUrl', evt.target.value)}
              placeholder="URL"
              type="url"
              value={attributes.linkUrl}
            />
            <CustomInput
              checked={attributes.linkNewTab}
              className="mt-1"
              id="rendering-hits-image-link-blank"
              label={t('openLinkNewTab')}
              onChange={() => onChange('linkNewTab', true)}
              type="radio"
            />
            <CustomInput
              checked={!attributes.linkNewTab}
              id="rendering-hits-image-link-no-blank"
              label={t('openLinkSameTab')}
              onChange={() => onChange('linkNewTab', false)}
              type="radio"
            />
          </FormGroup>
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default ImageRenderingHintsForm
