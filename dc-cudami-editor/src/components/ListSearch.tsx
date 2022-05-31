import './ListSearch.css'

import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {FaSearch} from 'react-icons/fa'
import {Button, Form, Input, InputGroup, InputGroupAddon} from 'reactstrap'

interface Props {
  isHighlighted: boolean
  onChange(value: string): void
  onSubmit(): void
  value: string
}

const ListSearch = ({isHighlighted, onChange, onSubmit, value}: Props) => {
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
              'pr-4',
              isHighlighted && ['border', 'border-danger'],
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
              'pr-2',
              {
                'd-none': !value,
              },
            )}
            close
            color="link"
            onClick={() => onChange('')}
          />
        </div>
        <InputGroupAddon addonType="append">
          <Button
            className="align-items-center d-flex"
            color="primary"
            type="submit"
          >
            <FaSearch />
          </Button>
        </InputGroupAddon>
      </InputGroup>
    </Form>
  )
}

export default ListSearch
