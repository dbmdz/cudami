import React, {useContext} from 'react'

import {getImageUrl} from './utils'
import {ApiContext} from '../api'

const PreviewImage = ({
  image,
  language,
  renderingHints = {},
  showCaption = false,
  width,
}) => {
  const {apiContextPath} = useContext(ApiContext)
  const {altText, caption, title} = renderingHints
  return (
    <figure className="mb-0 mx-auto" style={{maxWidth: `${width}px`}}>
      <img
        alt={altText?.[language] ?? ''}
        className="img-fluid mw-100"
        src={
          image
            ? getImageUrl(image, `${width},`)
            : `${apiContextPath}images/no-image.png`
        }
        title={title?.[language]}
      />
      {showCaption && caption?.[language] && (
        <figcaption>{caption[language]}</figcaption>
      )}
    </figure>
  )
}

export default PreviewImage
