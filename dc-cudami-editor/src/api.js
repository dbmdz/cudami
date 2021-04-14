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
  user: 'users',
  webpage: 'webpages',
  website: 'websites',
}

export async function addAttachedIdentifiable(
  contextPath,
  parentType,
  parentUuid,
  type,
  uuid
) {
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
  identifiables,
  parentType,
  parentUuid,
  type
) {
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

export async function changeUserStatus(contextPath, uuid, enabled) {
  const url = `${contextPath}api/users/${uuid}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify({enabled}),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin',
      },
      method: 'PATCH',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function findByIdentifier(contextPath, id, namespace, type) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/identifier/${namespace}:${id}`
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

export async function getIdentifierTypes(contextPath) {
  const url = `${contextPath}api/identifiertypes`
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
  parentType,
  parentUuid,
  type,
  pageNumber,
  pageSize,
  searchTerm
) {
  let url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = `${url}&searchTerm=${searchTerm}`
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, pageRequest, totalElements, searchTerm} = json
    return {
      content,
      pageSize: pageRequest.pageSize,
      totalElements,
      searchTerm,
    }
  } catch (err) {
    return {
      content: [],
      pageSize: 0,
      totalElements: 0,
      searchTerm: '',
    }
  }
}

export async function loadAvailableLanguages(contextPath) {
  const url = `${contextPath}api/languages`
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return []
  }
}

export async function loadDefaultLanguage(contextPath) {
  const url = `${contextPath}api/languages/default`
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return 'en'
  }
}

export async function loadIdentifiable(contextPath, type, uuid = 'new') {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/${uuid}`
  try {
    const result = await fetch(url)
    return result.json()
  } catch (err) {
    return {}
  }
}

export async function loadRootIdentifiables(
  contextPath,
  type,
  pageNumber,
  pageSize,
  searchTerm
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = `${url}&searchTerm=${searchTerm}`
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
  parentType,
  parentUuid,
  type,
  uuid
) {
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

export async function saveFileResource(contextPath, fileResource) {
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
  searchTerm,
  type,
  pageNumber = 0,
  pageSize = 10
) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/search?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
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
  searchTerm,
  pageNumber = 0,
  pageSize = 10
) {
  const url = `${contextPath}api/fileresources/type/${mediaType}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
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
  identifiables,
  parentType,
  parentUuid,
  type
) {
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

export async function updateFileResource(contextPath, fileResource) {
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

export async function uploadFile(contextPath, file, updateProgress) {
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
