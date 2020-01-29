import React from 'react'
import {Button, CustomInput, Form, FormGroup, Input, Label} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const ImageRenderingHintsForm = props => {
  const {t} = useTranslation()
  const {alignment, linkNewTab, linkUrl, width} = props.attributes
  return (
    <Form
      onSubmit={evt => {
        evt.preventDefault()
        props.onSubmit()
      }}
    >
      <FormGroup>
        <Label for="rendering-hints-alignment">Ausrichtung</Label>
        <div id="rendering-hints-alignment">
          <CustomInput
            checked={alignment === 'left'}
            id="rendering-hits-alignment-left"
            inline
            label="links"
            onChange={evt => props.onChange('alignment', evt.target.value)}
            type="radio"
            value="left"
          />
          <CustomInput
            checked={alignment === 'right'}
            id="rendering-hits-alignment-right"
            inline
            label="rechts"
            onChange={evt => props.onChange('alignment', evt.target.value)}
            type="radio"
            value="right"
          />
        </div>
      </FormGroup>
      <FormGroup>
        <Label for="rendering-hints-width">Breite</Label>
        <div id="rendering-hints-width">
          <CustomInput
            checked={width === '25%'}
            id="rendering-hits-width-25"
            inline
            label="25%"
            onChange={evt => props.onChange('width', evt.target.value)}
            type="radio"
            value="25%"
          />
          <CustomInput
            checked={width === '33%'}
            id="rendering-hits-width-33"
            inline
            label="33%"
            onChange={evt => props.onChange('width', evt.target.value)}
            type="radio"
            value="33%"
          />
          <CustomInput
            checked={width === '50%'}
            id="rendering-hits-width-50"
            inline
            label="50%"
            onChange={evt => props.onChange('width', evt.target.value)}
            type="radio"
            value="50%"
          />
          <CustomInput
            checked={width === '100%'}
            id="rendering-hits-width-100"
            inline
            label="100%"
            onChange={evt => props.onChange('width', evt.target.value)}
            type="radio"
            value="100%"
          />
        </div>
      </FormGroup>
      <FormGroup>
        <Label for="rendering-hints-image-link">Bild-Link</Label>
        <Input
          id="rendering-hints-image-link"
          onChange={evt => props.onChange('linkUrl', evt.target.value)}
          placeholder="URL"
          type="url"
          value={linkUrl}
        />
        <CustomInput
          checked={linkNewTab}
          className="mt-1"
          id="rendering-hits-image-link-blank"
          label="in neuem Fenster"
          onChange={() => props.onChange('linkNewTab', true)}
          type="radio"
        />
        <CustomInput
          checked={!linkNewTab}
          id="rendering-hits-image-link-no-blank"
          label="in gleichem Fenster"
          onChange={() => props.onChange('linkNewTab', false)}
          type="radio"
        />
      </FormGroup>
      <Button className="float-right" color="primary" type="submit">
        {t('add')}
      </Button>
    </Form>
  )
}

export default ImageRenderingHintsForm
