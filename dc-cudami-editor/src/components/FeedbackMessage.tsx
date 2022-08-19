import {Trans} from 'react-i18next'
import {FaTimes} from 'react-icons/fa'
import {Alert, Button} from 'reactstrap'

import {FeedbackMessage as Message} from '../types'

interface Props {
  /** a class name string to be used for the alert box, defaults to "mb-0" */
  className?: string
  message: Message
  /** if given a close button is rendered with the function as callback on click events */
  onClose?(): void
}

const FeedbackMessage = ({className = 'mb-0', message, onClose}: Props) => {
  const {color = 'info', key, links = [], text, values} = message
  const renderedLinks = links.map((link) => (
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
      {text ? (
        <span dangerouslySetInnerHTML={{__html: text}} />
      ) : (
        <Trans
          components={renderedLinks}
          i18nKey={`feedback:${key}`}
          values={values}
        />
      )}
    </Alert>
  )
}

export default FeedbackMessage
