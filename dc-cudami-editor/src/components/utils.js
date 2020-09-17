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
