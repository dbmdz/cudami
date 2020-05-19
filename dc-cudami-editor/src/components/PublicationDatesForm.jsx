import React, {Component} from 'react'
import {FormGroup, Label, Row, Col} from 'reactstrap'
import DatePicker from 'react-date-picker'
import {withTranslation} from 'react-i18next'

class PublicationDatesForm extends Component {
    state = {
      startPublicationDate: new Date(),
      endPublicationDate: new Date(),
    }

    startPublicationDateOnChange = (startPublicationDate) => {
      this.setState({ startPublicationDate });
    }

    endPublicationDateOnChange = (endPublicationDate) => {
      this.setState({ endPublicationDate });
    }

    constructor(props) {
      super(props)
      this.state = { startPublicationDate: null, endPublicationDate: null }
    }

    render() {
      const {t} = this.props
      const { startPublicationDate, endPublicationDate } = this.state
      
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
                onChange={this.startPublicationDateOnChange}
                value={startPublicationDate}
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
                onChange={this.endPublicationDateOnChange}
                value={endPublicationDate}
                yearAriaLabel={t('datePickerAriaLabel.yearAriaLabel')}
              />
            </Col>
          </Row>
        </FormGroup>
      )
    }
  }
  
export default withTranslation()(PublicationDatesForm)
