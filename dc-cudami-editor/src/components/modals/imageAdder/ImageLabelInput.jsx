import React from 'react'
import classNames from 'classnames'
import {FormGroup, Input, InputGroup, InputGroupAddon} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import InfoTooltip from '../../InfoTooltip'

const ImageLabelInput = ({
  className,
  label,
  name,
  onChange,
  toggleTooltip,
  tooltipName,
  tooltipsOpen,
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
          value={label ? Object.values(label)[0] : ''}
        />
        <InputGroupAddon addonType="append">
          <InfoTooltip
            isOpen={tooltipsOpen[tooltipName]}
            name={name}
            text={t('tooltips.label')}
            toggle={() => toggleTooltip(tooltipName)}
          />
        </InputGroupAddon>
      </InputGroup>
    </FormGroup>
  )
}

export default ImageLabelInput
