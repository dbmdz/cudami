import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {FormGroup, Input, InputGroup, InputGroupAddon} from 'reactstrap'

import InfoTooltip from '../../InfoTooltip'

const MediaLabelInput = ({className, label, name, onChange}) => {
  const {t} = useTranslation()
  return (
    <FormGroup className={classNames(['mb-0', className])}>
      <InputGroup>
        <Input
          name={name}
          onChange={(evt) => onChange(evt.target.value)}
          placeholder={t('label')}
          required
          type="text"
          value={label ? Object.values(label)[0] : ''}
        />
        <InputGroupAddon addonType="append">
          <InfoTooltip name={name} text={t('tooltips.label')} />
        </InputGroupAddon>
      </InputGroup>
    </FormGroup>
  )
}

export default MediaLabelInput
