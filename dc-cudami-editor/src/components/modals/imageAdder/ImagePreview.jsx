import React from 'react'

import {getImageUrl} from '../../utils'

const ImagePreview = ({iiifBaseUrl, filename, mimeType, uri}) => {
  const imageUrl = getImageUrl({iiifBaseUrl, mimeType, uri}, '250,')
  return (
    <figure className="d-block figure text-center">
      <img
        alt=""
        className="figure-img image-preview img-fluid"
        src={imageUrl}
      />
      {filename && (
        <figcaption className="figure-caption">{filename}</figcaption>
      )}
    </figure>
  )
}

export default ImagePreview
