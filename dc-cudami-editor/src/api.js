export const typeToEndpointMapping = {
  article: 'articles',
  collection: 'collections',
  corporateBody: 'corporatebodies',
  digitalObject: 'digitalobjects',
  entity: 'entities',
  fileResource: 'fileresources',
  geoLocation: 'geolocations',
  identifierType: 'identifiertypes',
  person: 'persons',
  project: 'projects',
  renderingTemplate: 'renderingtemplates',
  subcollection: 'subcollections',
  subtopic: 'subtopics',
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
    return await response.json()
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
    const response = await fetch(url)
    if (!response.ok) {
      return {}
    }
    return await response.json()
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
      method: 'DELETE',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function saveFileResource(contextPath, fileResource) {
  return await saveIdentifiable(contextPath, fileResource, 'fileResource')
}

export async function saveIdentifiable(
  contextPath,
  identifiable,
  type,
  {parentType, parentUuid} = {}
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}`
  if (parentType && parentUuid) {
    url = `${url}?parentType=${parentType}&parentUuid=${parentUuid}`
  }
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    })
    if (!response.ok) {
      throw Error(response.statusText)
    }
    return await response.json()
  } catch (err) {
    return {error: true}
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
      },
      method: 'PUT',
    })
    return response.ok
  } catch (err) {
    return false
  }
}

export async function updateFileResource(contextPath, fileResource) {
  return await updateIdentifiable(contextPath, fileResource, 'fileResource')
}

export async function updateIdentifiable(contextPath, identifiable, type) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/${identifiable.uuid}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'PUT',
    })
    if (!response.ok) {
      throw Error(response.statusText)
    }
    return await response.json()
  } catch (err) {
    return {error: err}
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

// Users are special so lets treat them special
/**
Update an existing user

@param contextPath
@param user User object as retrieved from server
@param passwords `{pwd1:..., pwd2:...}` that will be added as request params
@returns `{status: status code of request, returnObject: on error an error object, the new user object otherwise}`
*/
export async function updateUser(contextPath, user, passwords) {
  let url = `${contextPath}api/${typeToEndpointMapping["user"]}/${user.uuid}`
  if (passwords) {
    const paramList = Object.entries(passwords).map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
    url += `?${paramList.join("&")}`
  }
  let result
  try {
    result = await fetch(url, {
      body: JSON.stringify(user),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'PUT'
    }).then(async (response) => ({status: response.status, returnObject: await response.json()}))
  } catch (err) {
    result = {status: 500, returnObject: null}
  }
  return result
}
