import {useTranslation} from 'react-i18next'
import {Button, ButtonGroup, Col, Row} from 'reactstrap'

interface Props {
  buttonsDisabled?: boolean
  formId: string
  heading: string
}

const Header = ({buttonsDisabled = false, formId, heading}: Props) => {
  const {t} = useTranslation()
  return (
    <div className="border content-header mx-n1 mb-2 p-1 sticky-top">
      <Row form>
        <Col xs="6" sm="9">
          <h1>{heading}</h1>
        </Col>
        <Col xs="6" sm="3">
          <ButtonGroup className="float-right">
            <Button
              color="primary"
              disabled={buttonsDisabled}
              form={formId}
              type="submit"
            >
              {t('save')}
            </Button>
          </ButtonGroup>
        </Col>
      </Row>
    </div>
  )
}

export default Header
