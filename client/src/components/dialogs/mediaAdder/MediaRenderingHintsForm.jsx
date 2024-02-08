import {useTranslation} from 'react-i18next'
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Collapse,
  CustomInput,
  FormGroup,
  Label,
} from 'reactstrap'

import InputWithFloatingLabel from '../../InputWithFloatingLabel'

const MediaRenderingHintsForm = ({
  alignment,
  enableAlignment = true,
  enableLink = true,
  enableWidth = true,
  isOpen,
  linkNewTab,
  linkUrl,
  mediaType,
  onChange,
  toggle,
  width,
}) => {
  const {t} = useTranslation()
  return (
    <Card className="media-adder-content media-rendering-hints">
      <CardHeader>
        <Button className="font-weight-bold p-0" color="link" onClick={toggle}>
          {t('defineRenderingHints')}
        </Button>
      </CardHeader>
      <Collapse isOpen={isOpen}>
        <CardBody>
          {enableAlignment && (
            <FormGroup>
              <Label
                className="font-weight-bold"
                for="rendering-hints-alignment"
              >
                {t('alignment')}
              </Label>
              <div id="rendering-hints-alignment">
                <CustomInput
                  checked={alignment === 'left'}
                  id="rendering-hits-alignment-left"
                  inline
                  label={t('alignmentLeft')}
                  onChange={(evt) => onChange('alignment', evt.target.value)}
                  type="radio"
                  value="left"
                />
                <CustomInput
                  checked={alignment === 'right'}
                  id="rendering-hits-alignment-right"
                  inline
                  label={t('alignmentRight')}
                  onChange={(evt) => onChange('alignment', evt.target.value)}
                  type="radio"
                  value="right"
                />
                <CustomInput
                  checked={alignment === ''}
                  id="rendering-hits-no-alignment"
                  inline
                  label={t('noAlignment')}
                  onChange={() => onChange('alignment', '')}
                  type="radio"
                />
              </div>
            </FormGroup>
          )}
          {enableWidth && (
            <FormGroup>
              <Label className="font-weight-bold" for="rendering-hints-width">
                {t('width')}
              </Label>
              <div id="rendering-hints-width">
                <CustomInput
                  checked={width === '25%'}
                  id="rendering-hits-width-25"
                  inline
                  label="25%"
                  onChange={(evt) => onChange('width', evt.target.value)}
                  type="radio"
                  value="25%"
                />
                <CustomInput
                  checked={width === '33%'}
                  id="rendering-hits-width-33"
                  inline
                  label="33%"
                  onChange={(evt) => onChange('width', evt.target.value)}
                  type="radio"
                  value="33%"
                />
                <CustomInput
                  checked={width === '50%'}
                  id="rendering-hits-width-50"
                  inline
                  label="50%"
                  onChange={(evt) => onChange('width', evt.target.value)}
                  type="radio"
                  value="50%"
                />
                <CustomInput
                  checked={width === '100%'}
                  id="rendering-hits-width-100"
                  inline
                  label="100%"
                  onChange={(evt) => onChange('width', evt.target.value)}
                  type="radio"
                  value="100%"
                />
              </div>
            </FormGroup>
          )}
          {enableLink && (
            <FormGroup>
              <Label className="font-weight-bold">
                {t(`${mediaType}Link`)}
              </Label>
              <InputWithFloatingLabel
                label="URL"
                name={`${mediaType}-link`}
                onChange={(value) => onChange('linkUrl', value)}
                type="url"
                value={linkUrl}
              />
              <CustomInput
                checked={linkNewTab}
                className="mt-1"
                id={`${mediaType}-link-blank`}
                label={t('openLinkNewTab')}
                onChange={() => onChange('linkNewTab', true)}
                type="radio"
              />
              <CustomInput
                checked={!linkNewTab}
                id={`${mediaType}-link-no-blank`}
                label={t('openLinkSameTab')}
                onChange={() => onChange('linkNewTab', false)}
                type="radio"
              />
            </FormGroup>
          )}
        </CardBody>
      </Collapse>
    </Card>
  )
}

export default MediaRenderingHintsForm
