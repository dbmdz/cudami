export function getImageUrl(image, width = 'full') {
  const mimeExtensionMapping = {
    gif: 'gif',
    png: 'png',
  }
  const subMimeType = image.mimeType.split('/')[1]
  return image.iiifBaseUrl
    ? `${image.iiifBaseUrl}/full/${width}/0/default.${
        mimeExtensionMapping[subMimeType] ?? 'jpg'
      }`
    : image.uri
}
