import React, {Component} from 'react'
import {FormGroup, Label, Row, Col} from 'reactstrap'
import DatePicker from 'react-date-picker'
import {useTranslation} from 'react-i18next'

const PublicationDatesForm = (props) => {
  const {t} = useTranslation()

  return (
    <FormGroup>
      <Row>
        <Col>
          <Label className="font-weight-bold" for="publication-start-date">
            {t('startPublicationDate')}:
          </Label>
        </Col>
        <Col>
          <DatePicker
            calendarAriaLabel={t('datePicker.toggleCalendar')}
            clearAriaLabel={t('datePicker.clearDate')}
            dayAriaLabel={t('datePicker.day')}
            id={"publication-start-date"}
            monthAriaLabel={t('datePicker.month')}
            nativeInputAriaLabel={t('datePicker.date')}
            onChange={(date) =>
              props.onChange(
                'publicationStart',
                date
                  ? date.getFullYear() +
                      '-' +
                      ('0' + (date.getMonth() + 1)).slice(-2) +
                      '-' +
                      ('0' + date.getDate()).slice(-2)
                  : undefined
              )
            }
            required={false}
            value={
              props.publicationStartDate && new Date(props.publicationStartDate)
            }
            yearAriaLabel={t('datePicker.year')}
          />
        </Col>
        <Col>
          <Label className="font-weight-bold" for="publication-end-date">
            {t('endPublicationDate')}:
          </Label>
        </Col>
        <Col>
          <DatePicker
            calendarAriaLabel={t('datePicker.toggleCalendar')}
            clearAriaLabel={t('datePicker.clearDate')}
            dayAriaLabel={t('datePicker.day')}
            id={"publication-end-date"}
            monthAriaLabel={t('datePicker.month')}
            nativeInputAriaLabel={t('datePicker.date')}
            onChange={(date) =>
              props.onChange(
                'publicationEnd',
                date
                  ? date.getFullYear() +
                      '-' +
                      ('0' + (date.getMonth() + 1)).slice(-2) +
                      '-' +
                      ('0' + date.getDate()).slice(-2)
                  : undefined
              )
            }
            required={false}
            value={
              props.publicationEndDate && new Date(props.publicationEndDate)
            }
            yearAriaLabel={t('datePicker.year')}
          />
        </Col>
      </Row>
    </FormGroup>
  )
}

export default PublicationDatesForm
