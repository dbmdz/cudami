export function getImageUrl(image, width = 'full') {
  if (!image.iiifBaseUrl || !image.mimeType) {
    return image.uri
  }
  const mimeExtensionMapping = {
    gif: 'gif',
    png: 'png',
  }
  const subMimeType = image.mimeType.split('/')[1]
  return `${image.iiifBaseUrl}/full/${width}/0/default.${
    mimeExtensionMapping[subMimeType] ?? 'jpg'
  }`
}
