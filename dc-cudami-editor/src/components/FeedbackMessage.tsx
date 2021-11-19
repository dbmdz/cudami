import {Trans} from 'react-i18next'
import {FaTimes} from 'react-icons/fa'
import {Alert, Button} from 'reactstrap'

interface Message {
  /** the color of the message */
  color?: string
  /** the translation key to use */
  key: string
  /** links that can be used in the translation string */
  links?: string[]
  /** a simple text to display instead of translated content */
  text?: string
  /** an key-value mapping of stuff to be interpolated into the translation string */
  values?: Record<string, unknown>
}

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

export type {Message}
export default FeedbackMessage
