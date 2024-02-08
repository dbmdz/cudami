import classNames from 'classnames'
import {useTranslation} from 'react-i18next'
import {FormGroup, Input, InputGroup, InputGroupAddon} from 'reactstrap'

import InfoTooltip from '../../InfoTooltip'
import {getLabelValue} from '../../utils'

const MediaLabelInput = ({
  activeLanguage,
  className,
  defaultLanguage,
  label,
  name,
  onChange,
}) => {
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
          value={getLabelValue(label, activeLanguage, defaultLanguage)}
        />
        <InputGroupAddon addonType="append">
          <InfoTooltip name={name} text={t('tooltips.label')} />
        </InputGroupAddon>
      </InputGroup>
    </FormGroup>
  )
}

export default MediaLabelInput
