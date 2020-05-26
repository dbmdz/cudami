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
            calendarAriaLabel={t('datePickerAriaLabel.calendarAriaLabel')}
            clearAriaLabel={t('datePickerAriaLabel.clearAriaLabel')}
            dayAriaLabel={t('datePickerAriaLabel.dayAriaLabel')}
            monthAriaLabel={t('datePickerAriaLabel.monthAriaLabel')}
            nativeInputAriaLabel={t('datePickerAriaLabel.nativeInputAriaLabel')}
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
            yearAriaLabel={t('datePickerAriaLabel.yearAriaLabel')}
          />
        </Col>
        <Col>
          <Label className="font-weight-bold" for="publication-end-date">
            {t('endPublicationDate')}:
          </Label>
        </Col>
        <Col>
          <DatePicker
            calendarAriaLabel={t('datePickerAriaLabel.calendarAriaLabel')}
            clearAriaLabel={t('datePickerAriaLabel.clearAriaLabel')}
            dayAriaLabel={t('datePickerAriaLabel.dayAriaLabel')}
            monthAriaLabel={t('datePickerAriaLabel.monthAriaLabel')}
            nativeInputAriaLabel={t('datePickerAriaLabel.nativeInputAriaLabel')}
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
            yearAriaLabel={t('datePickerAriaLabel.yearAriaLabel')}
          />
        </Col>
      </Row>
    </FormGroup>
  )
}

export default PublicationDatesForm
