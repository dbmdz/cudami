import './IdentifiableSearch.css'

import classNames from 'classnames'
import React from 'react'
import {useTranslation} from 'react-i18next'
import {FaSearch, FaTimes} from 'react-icons/fa'
import {Button, Form, Input, InputGroup, InputGroupAddon} from 'reactstrap'

const IdentifiableSearch = ({onChange, onSubmit, value, isHighlighted}) => {
  const {t} = useTranslation()
  return (
    <Form
      onSubmit={(evt) => {
        evt.preventDefault()
        onSubmit()
      }}
    >
      <InputGroup className="flex-nowrap">
        <div className="position-relative">
          <Input
            className={classNames(
              'pr-5',
              isHighlighted && ['border', 'border-danger', 'border-width-3']
            )}
            onChange={(evt) => onChange(evt.target.value)}
            placeholder={t('searchTerm')}
            type="text"
            value={value}
          />
          <Button
            className={classNames(
              'position-absolute',
              'position-centered',
              'mr-0',
              'bg-transparent',
              'border-0',
              !value && 'd-none'
            )}
            type="button"
            onClick={() => onChange('')}
          >
            <FaTimes color="gray" />
          </Button>
        </div>
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
