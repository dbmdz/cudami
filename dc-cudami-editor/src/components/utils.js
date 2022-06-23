export function formatDate(date, locale, onlyDate = false) {
  const dateToFormat = date instanceof Date ? date : new Date(date)
  const options = {
    day: '2-digit',
    hour12: false,
    month: '2-digit',
    year: 'numeric',
  }
  if (onlyDate) {
    return dateToFormat.toLocaleDateString(locale, options)
  }
  return dateToFormat.toLocaleString(locale, {
    ...options,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

export function getImageUrl(image, width = 'full') {
  if (!image.httpBaseUrl || !image.mimeType) {
    return image.uri
  }
  const mimeExtensionMapping = {
    gif: 'gif',
    png: 'png',
  }
  const subMimeType = image.mimeType.split('/')[1]
  return `${image.httpBaseUrl}/full/${width}/0/default.${
    mimeExtensionMapping[subMimeType] ?? 'jpg'
  }`
}

export function getLabelValue(label, activeLanguage, defaultLanguage) {
  return (
    label[activeLanguage] ?? label[defaultLanguage] ?? Object.values(label)[0]
  )
}

export function getMediaUrl(fileResource, mediaType) {
  switch (mediaType) {
    case 'image':
      return getImageUrl(fileResource)
    case 'video':
      return getVideoUrl(fileResource)
    default:
      return null
  }
}

export function getVideoUrl(video) {
  if (!video.httpBaseUrl || !video.mimeType) {
    return video.uri
  }
  return `${video.httpBaseUrl}/default.mp4`
}
