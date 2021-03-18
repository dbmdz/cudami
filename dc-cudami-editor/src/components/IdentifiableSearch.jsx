import React from 'react'
import {useTranslation} from 'react-i18next'
import {FaSearch} from 'react-icons/fa'
import {Button, Form, Input, InputGroup, InputGroupAddon} from 'reactstrap'

const IdentifiableSearch = ({onChange, onSubmit, value}) => {
  const {t} = useTranslation()
  return (
    <Form
      onSubmit={(evt) => {
        evt.preventDefault()
        onSubmit()
      }}
    >
      <InputGroup>
        <Input
          onChange={(evt) => onChange(evt.target.value)}
          placeholder={t('searchTerm')}
          type="text"
          value={value}
        />
        <InputGroupAddon addonType="append">
          <Button color="primary" type="submit">
            <FaSearch />
          </Button>
        </InputGroupAddon>
      </InputGroup>
    </Form>
  )
}

export default IdentifiableSearch
