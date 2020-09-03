import React from 'react'
import classNames from 'classnames'
import {
  Button,
  FormGroup,
  Input,
  InputGroup,
  InputGroupAddon,
  Popover,
  PopoverBody,
} from 'reactstrap'
import {useTranslation} from 'react-i18next'
import {FaQuestionCircle} from 'react-icons/fa'

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
          <Button
            className="border"
            color="light"
            id={`${name}-tooltip`}
            type="button"
          >
            <FaQuestionCircle />
          </Button>
          <Popover
            isOpen={tooltipsOpen[tooltipName]}
            placement="left"
            target={`${name}-tooltip`}
            toggle={() => toggleTooltip(tooltipName)}
          >
            <PopoverBody>{t('tooltips.label')}</PopoverBody>
          </Popover>
        </InputGroupAddon>
      </InputGroup>
    </FormGroup>
  )
}

export default ImageLabelInput
