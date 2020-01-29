import React from 'react'
import {Button, Form, FormGroup, Input} from 'reactstrap'
import {useTranslation} from 'react-i18next'

const ImageMetadataForm = props => {
  const {t} = useTranslation()
  const {altText, caption, title} = props.attributes
  return (
    <Form
      onSubmit={evt => {
        evt.preventDefault()
        props.onSubmit()
      }}
    >
      <FormGroup>
        <Input
          onChange={evt => props.onChange('caption', evt.target.value)}
          placeholder={t('caption')}
          type="text"
          value={caption}
        />
      </FormGroup>
      <FormGroup>
        <Input
          onChange={evt => props.onChange('title', evt.target.value)}
          placeholder={t('title')}
          type="text"
          value={title}
        />
      </FormGroup>
      <FormGroup>
        <Input
          onChange={evt => props.onChange('altText', evt.target.value)}
          placeholder={t('altText')}
          type="text"
          value={altText}
        />
      </FormGroup>
      <Button className="float-right" color="primary" type="submit">
        {t('next')}
      </Button>
    </Form>
  )
}

export default ImageMetadataForm
