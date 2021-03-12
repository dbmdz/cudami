export const typeToEndpointMapping = {
  article: 'articles',
  collection: 'collections',
  corporateBody: 'corporatebodies',
  digitalObject: 'digitalobjects',
  fileResource: 'fileresources',
  geoLocation: 'geolocations',
  identifierType: 'identifiertypes',
  person: 'persons',
  project: 'projects',
  renderingTemplate: 'renderingtemplates',
  subcollection: 'subcollections',
  topic: 'topics',
  webpage: 'webpages',
  website: 'websites',
}

export async function addAttachedIdentifiable(
  contextPath,
  mock,
  parentType,
  parentUuid,
  type,
  uuid
) {
  if (mock) {
    return true
  }
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}/${uuid}`
  try {
    const response = await fetch(url, {
      headers: {
        credentials: 'same-origin',
      },
      method: 'POST',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function addAttachedIdentifiables(
  contextPath,
  mock,
  identifiables,
  parentType,
  parentUuid,
  type
) {
  if (mock) {
    return true
  }
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiables),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin',
      },
      method: 'POST',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function findByIdentifier(contextPath, mock, id, namespace, type) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}/identifier/${namespace}:${id}`
  if (mock) {
    url = `/__mock__/${type}.json`
  }
  try {
    const response = await fetch(url)
    if (!response.ok) {
      return {}
    }
    const json = await response.json()
    return json
  } catch (err) {
    return {}
  }
}

export async function getIdentifierTypes(contextPath, mock) {
  let url = `${contextPath}api/identifiertypes`
  if (mock) {
    url = '/__mock__/identifierTypes.json'
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    return json.content
  } catch (err) {
    return []
  }
}

export async function loadAttachedIdentifiables(
  contextPath,
  mock,
  parentType,
  parentUuid,
  type,
  pageNumber,
  pageSize,
  searchTerm
) {
  let url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = url.concat("&searchTerm=", searchTerm)
  }
  if (mock) {
    url = `/__mock__/${type}s.json`
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, pageRequest, totalElements} = json
    return {
      content,
      pageSize: pageRequest.pageSize,
      totalElements,
    }
  } catch (err) {
    return {
      content: [],
      pageSize: 0,
      totalElements: 0,
    }
  }
}

export async function loadAvailableLanguages(contextPath, mock) {
  if (mock) {
    return ['es', 'fr']
  }
  const url = `${contextPath}api/languages`
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return []
  }
}

export async function loadDefaultLanguage(contextPath, mock) {
  if (mock) {
    return 'en'
  }
  const url = `${contextPath}api/languages/default`
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return 'en'
  }
}

export async function loadIdentifiable(contextPath, mock, type, uuid = 'new') {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}/${uuid}`
  if (mock) {
    url =
      uuid === 'new' ? `/__mock__/new/${type}.json` : `/__mock__/${type}.json`
  }
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return {}
  }
}

export async function loadRootIdentifiables(
  contextPath,
  mock,
  type,
  pageNumber,
  pageSize,
  searchTerm
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = url.concat("?searchTerm=", searchTerm)
  }
  if (mock) {
    url = `/__mock__/${type}s.json`
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, pageRequest, totalElements} = json
    return {
      content,
      pageSize: pageRequest.pageSize,
      totalElements,
    }
  } catch (err) {
    return {
      content: [],
      pageSize: 0,
      totalElements: 0,
    }
  }
}

export async function removeAttachedIdentifiable(
  contextPath,
  mock,
  parentType,
  parentUuid,
  type,
  uuid
) {
  if (mock) {
    return true
  }
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}/${uuid}`
  try {
    const response = await fetch(url, {
      headers: {
        credentials: 'same-origin',
      },
      method: 'DELETE',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function saveFileResource(contextPath, fileResource, mock) {
  if (mock) {
    const result = await fetch('/__mock__/fileResource.json')
    return result.json()
  }
  const savedFileResource = await saveIdentifiable(
    contextPath,
    fileResource,
    null,
    null,
    'fileResource',
    false
  )
  return savedFileResource
}

export async function saveIdentifiable(
  contextPath,
  identifiable,
  parentType,
  parentUuid,
  type,
  redirect = true
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}/new`
  if (parentType && parentUuid) {
    url = `${url}?parentType=${parentType}&parentUuid=${parentUuid}`
  }
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin',
      },
      method: 'POST',
    })
    const json = await response.json()
    if (redirect) {
      const viewUrl = `${contextPath}${typeToEndpointMapping[type]}/${json.uuid}`
      window.location.href = viewUrl
    } else {
      return json
    }
  } catch (err) {
    console.log('An error occured while saving the identifiable')
  }
}

export async function searchIdentifiables(
  contextPath,
  mock,
  searchTerm,
  type,
  pageNumber = 0,
  pageSize = 10
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}/search?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
  if (mock) {
    url = `/__mock__/${type}s.json`
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    return {
      suggestions: json.content,
      totalElements: json.totalElements,
    }
  } catch (err) {
    return {
      suggestions: [],
      totalElements: 0,
    }
  }
}

export async function searchMedia(
  contextPath,
  mediaType,
  mock,
  searchTerm,
  pageNumber = 0,
  pageSize = 10
) {
  let url = `${contextPath}api/fileresources/type/${mediaType}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
  if (mock) {
    url = `/__mock__/${mediaType}s.json`
  }
  try {
    const result = await fetch(url)
    const json = await result.json()
    return {
      suggestions: json.content,
      totalElements: json.totalElements,
    }
  } catch (err) {
    return {
      suggestions: [],
      totalElements: 0,
    }
  }
}

export async function updateAttachedIdentifiablesOrder(
  contextPath,
  mock,
  identifiables,
  parentType,
  parentUuid,
  type
) {
  if (mock) {
    return true
  }
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiables),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin',
      },
      method: 'PUT',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function updateFileResource(contextPath, fileResource, mock) {
  if (mock) {
    const result = await fetch('/__mock__/fileResource.json')
    return result.json()
  }
  const updatedFileResource = await updateIdentifiable(
    contextPath,
    fileResource,
    'fileResource',
    false
  )
  return updatedFileResource
}

export async function updateIdentifiable(
  contextPath,
  identifiable,
  type,
  redirect = true
) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/${identifiable.uuid}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin',
      },
      method: 'PUT',
    })
    const json = await response.json()
    if (redirect) {
      const viewUrl = `${contextPath}${typeToEndpointMapping[type]}/${json.uuid}`
      window.location.href = viewUrl
    } else {
      return json
    }
  } catch (err) {
    console.log('An error occured while updating the identifiable')
  }
}

export async function uploadFile(contextPath, file, mock, updateProgress) {
  if (mock) {
    const result = await fetch('/__mock__/fileResource.json')
    updateProgress(100)
    return result.json()
  }
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest()
    for (let eventType of ['abort', 'error', 'timeout']) {
      request.addEventListener(eventType, () => reject(request.statusText))
    }

    request.addEventListener('load', () => {
      resolve(JSON.parse(request.response))
    })

    request.upload.addEventListener('progress', (evt) => {
      if (evt.lengthComputable) {
        updateProgress(Math.round((evt.loaded / evt.total) * 100))
      }
    })

    request.open('POST', `${contextPath}api/files`, true)
    const formData = new FormData()
    formData.append('userfile', file, file.name)
    request.send(formData)
  })
}
