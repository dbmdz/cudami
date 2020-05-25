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
            onChange={props.onChange("publicationStart", props.identifiable.publicationStart || null)}
            value={props.identifiable.publicationEnd}
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
            onChange={props.onChange("publicationEnd", props.identifiable.publicationEnd || null)}
            value={props.identifiable.publicationEnd}
            yearAriaLabel={t('datePickerAriaLabel.yearAriaLabel')}
          />
        </Col>
      </Row>
    </FormGroup>
  )
}
  
export default PublicationDatesForm
