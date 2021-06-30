import './DateInput.css'

import {useContext} from 'react'
import DatePicker from 'react-date-picker'
import {useTranslation} from 'react-i18next'
import {FormGroup, Label} from 'reactstrap'

import AppContext from './AppContext'

const formatDate = (date) => {
  if (date) {
    // Selected datetime will be displayed correctly in DatePicker.
    // But after reloading, there's a minus one-day offset. Here
    // we add 12 hours (could be related to the time zone of the user)
    // to the user-defined date to workaround this issue 😒
    date.setHours(date.getHours() + 12)
    return date.toISOString().slice(0, 10)
  }
  return undefined
}

const DateInput = ({id, label, onChange, value}) => {
  const {uiLocale} = useContext(AppContext)
  const {t} = useTranslation()
  return (
    <FormGroup>
      <Label className="d-block font-weight-bold" for={id}>
        {label}
      </Label>
      <DatePicker
        calendarAriaLabel={t('datePicker:toggleCalendar')}
        calendarClassName="rounded"
        className="form-control"
        clearAriaLabel={t('datePicker:clearDate')}
        dayAriaLabel={t('datePicker:day')}
        id={id}
        locale={uiLocale}
        monthAriaLabel={t('datePicker:month')}
        nativeInputAriaLabel={t('datePicker:date')}
        onChange={(date) => onChange(formatDate(date))}
        required={false}
        value={value && new Date(value)}
        yearAriaLabel={t('datePicker:year')}
      />
    </FormGroup>
  )
}

export default DateInput
