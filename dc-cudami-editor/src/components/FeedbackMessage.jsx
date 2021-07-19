import {Trans} from 'react-i18next'
import {FaTimes} from 'react-icons/fa'
import {Alert, Button} from 'reactstrap'

/**
@field className defaults to "mb-0"
@field message `{color: defaults to "info", key: used in t for `feedback:${key}`, links: for <a> tag, values: object used in interpolated message strings}`
@field onClose
*/
const FeedbackMessage = ({className = 'mb-0', message, onClose}) => {
  let {color = 'info', key, links = [], values} = message
  links = links.map((link) => (
    <a
      href={link}
      key={link}
      style={{color: 'inherit', textDecoration: 'underline'}}
    ></a>
  ))
  return (
    <Alert className={className} color={color}>
      {onClose && (
        <Button
          className="close"
          color="link"
          onClick={onClose}
          style={{color: 'inherit'}}
        >
          <FaTimes size={16} />
        </Button>
      )}
      <Trans components={links} i18nKey={`feedback:${key}`} values={values} />
    </Alert>
  )
}

export default FeedbackMessage
