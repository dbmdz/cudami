import './IdentifiableSearch.css'

import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {FaSearch, FaTimes} from 'react-icons/fa'
import {Button, Form, Input, InputGroup, InputGroupAddon} from 'reactstrap'

const IdentifiableSearch = ({isHighlighted, onChange, onSubmit, value}) => {
  const {t} = useTranslation()
  return (
    <Form
      className="mb-1"
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
              isHighlighted && ['border', 'border-danger'],
            )}
            onChange={(evt) => onChange(evt.target.value)}
            placeholder={t('searchTerm')}
            type="text"
            value={value}
          />
          <Button
            className={classNames('position-absolute', 'position-centered', {
              'd-none': !value,
            })}
            color="link"
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
