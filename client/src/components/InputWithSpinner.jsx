import classNames from 'classnames'
import {
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
  Spinner,
} from 'reactstrap'

const InputWithSpinner = ({children, inputProps, loading}) => (
  <InputGroup>
    <Input
      className={classNames({'border-right-0': loading})}
      {...inputProps}
    />
    {loading && (
      <InputGroupAddon addonType="append">
        <InputGroupText className="bg-white">
          <Spinner color="secondary" size="sm" />
        </InputGroupText>
      </InputGroupAddon>
    )}
    {children}
  </InputGroup>
)

export default InputWithSpinner
