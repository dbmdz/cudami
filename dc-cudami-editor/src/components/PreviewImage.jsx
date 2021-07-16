import {useContext} from 'react'
import {useTranslation} from 'react-i18next'

import AppContext from './AppContext'
import {getImageUrl} from './utils'

const PreviewImage = ({
  className = 'mb-0 mx-auto',
  image,
  language,
  renderingHints = {},
  showCaption = false,
  width,
}) => {
  const {t} = useTranslation()
  const {apiContextPath, defaultLanguage} = useContext(AppContext)
  if (!language) {
    language = defaultLanguage
  }
  const {altText, caption, title} = renderingHints
  return (
    <figure className={className} style={{maxWidth: `${width}px`}}>
      <img
        alt={
          image ? altText?.[language] ?? image.filename : t('noPreviewImage')
        }
        className="img-fluid mw-100"
        src={
          image
            ? getImageUrl(image, `${width},`)
            : `${apiContextPath}images/no-image.png`
        }
        title={title?.[language]}
      />
      {showCaption && caption?.[language] && (
        <figcaption className="figure-caption">{caption[language]}</figcaption>
      )}
    </figure>
  )
}

export default PreviewImage
