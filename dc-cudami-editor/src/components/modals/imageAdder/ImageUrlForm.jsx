import React from 'react'
import {Button, Form, FormGroup, Input} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const ImageMetadataForm = props => {
  const {t} = useTranslation()
  const {label, url} = props.attributes
  return (
    <Form
      onSubmit={evt => {
        evt.preventDefault()
        props.onSubmit()
      }}
    >
      <FormGroup>
        <Input
          onChange={evt => props.onChange('url', evt.target.value)}
          placeholder="URL"
          readOnly={props.readOnly && true}
          required
          type="url"
          value={url}
        />
      </FormGroup>
      <FormGroup>
        <Input
          onChange={evt => props.onChange('label', evt.target.value)}
          placeholder={t('label')}
          readOnly={props.readOnly && true}
          required
          type="text"
          value={label}
        />
      </FormGroup>
      <Button className="float-right" color="primary" type="submit">
        {t('next')}
      </Button>
    </Form>
  )
}

export default ImageMetadataForm
