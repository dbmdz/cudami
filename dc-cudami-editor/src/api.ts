export const typeToEndpointMapping: Record<string, string> = {
  article: 'articles',
  collection: 'collections',
  config: 'config',
  corporateBody: 'corporatebodies',
  digitalObject: 'digitalobjects',
  entity: 'entities',
  file: 'files',
  fileResource: 'fileresources',
  geoLocation: 'geolocations',
  identifierType: 'identifiertypes',
  language: 'languages',
  person: 'persons',
  project: 'projects',
  renderingTemplate: 'renderingtemplates',
  subcollection: 'subcollections',
  subtopic: 'subtopics',
  topic: 'topics',
  urlAlias: 'urlaliases',
  user: 'users',
  webpage: 'webpages',
  website: 'websites',
}

export async function addAttachedIdentifiable(
  contextPath: string,
  parentType: string,
  parentUuid: string,
  type: string,
  uuid: string,
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
  contextPath: string,
  identifiables: any[],
  parentType: string,
  parentUuid: string,
  type: string,
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

export async function changeUserStatus(
  contextPath: string,
  uuid: string,
  enabled: boolean,
) {
  const url = `${contextPath}api/${typeToEndpointMapping.user}/${uuid}`
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

export async function findByIdentifier(
  contextPath: string,
  id: string,
  namespace: string,
  type: string,
) {
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

export async function generateSlug(
  contextPath: string,
  language: string,
  slug: string,
  websiteUuid: string,
) {
  let url = `${contextPath}api/${
    typeToEndpointMapping.urlAlias
  }/slug/${language}/${encodeURIComponent(slug)}`
  if (websiteUuid) {
    url += `/${websiteUuid}`
  }
  try {
    const response = await fetch(url)
    return await response.json()
  } catch (err) {
    return slug
  }
}

export async function getConfig(contextPath: string) {
  const url = `${contextPath}api/${typeToEndpointMapping.config}`
  try {
    const response = await fetch(url)
    const json = await response.json()
    return json
  } catch (err) {
    return {}
  }
}

export async function getIdentifierTypes(contextPath: string) {
  const url = `${contextPath}api/${typeToEndpointMapping.identifierType}`
  try {
    const response = await fetch(url)
    const json = await response.json()
    return json.content
  } catch (err) {
    return []
  }
}

export async function loadAttachedIdentifiables(
  contextPath: string,
  parentType: string,
  parentUuid: string,
  type: string,
  pageNumber: number,
  pageSize: number,
  searchTerm: string,
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

export async function loadAvailableLanguages(contextPath: string) {
  const url = `${contextPath}api/${typeToEndpointMapping.language}`
  try {
    const response = await fetch(url)
    return await response.json()
  } catch (err) {
    return []
  }
}

export async function loadDefaultLanguage(contextPath: string) {
  const url = `${contextPath}api/${typeToEndpointMapping.language}/default`
  try {
    const response = await fetch(url)
    return await response.json()
  } catch (err) {
    return 'en'
  }
}

export async function loadIdentifiable(
  contextPath: string,
  type: string,
  uuid = 'new',
) {
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
  contextPath: string,
  type: string,
  pageNumber: number,
  pageSize: number,
  searchTerm: string,
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

export async function loadUser(
  contextPath: string,
  {admin = false, uuid = 'new'},
) {
  const url = `${contextPath}api/${typeToEndpointMapping.user}/${uuid}?admin=${admin}`
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

export async function removeAttachedIdentifiable(
  contextPath: string,
  parentType: string,
  parentUuid: string,
  type: string,
  uuid: string,
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

export async function saveFileResource(contextPath: string, fileResource: any) {
  return await saveIdentifiable(contextPath, fileResource, 'fileResource')
}

export async function saveIdentifiable(
  contextPath: string,
  identifiable: any,
  type: string,
  {parentType, parentUuid}: Record<string, string> = {},
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

/**
 * Update an existing user or save a new one.
 *
 * @param contextPath
 * @param user User object as retrieved from server
 * @param passwords `{pwd1:..., pwd2:...}` that will be added as request params
 * @returns `{error: flag if there was an error, json: on error an error object, the new/updated user object otherwise}`
 */
export async function saveOrUpdateUser(
  contextPath: string,
  user: any,
  passwords: Record<string, string>,
) {
  let url = `${contextPath}api/${typeToEndpointMapping.user}`
  if (user.uuid) {
    url += `/${user.uuid}`
  }
  if (passwords) {
    const paramList = Object.entries(passwords).map(
      ([k, v]) => `${k}=${encodeURIComponent(v)}`,
    )
    url += `?${paramList.join('&')}`
  }
  try {
    const response = await fetch(url, {
      body: JSON.stringify(user),
      headers: {
        'Content-Type': 'application/json',
      },
      method: user.uuid ? 'PUT' : 'POST',
    })
    const json = await response.json()
    return {
      error: !response.ok,
      json,
    }
  } catch (err) {
    return {
      error: true,
      json: {},
    }
  }
}

export async function searchIdentifiables(
  contextPath: string,
  searchTerm: string,
  type: string,
  pageNumber = 0,
  pageSize = 10,
) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/search?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, totalElements} = json
    return {
      content,
      totalElements,
    }
  } catch (err) {
    return {
      content: [],
      totalElements: 0,
    }
  }
}

export async function searchMedia(
  contextPath: string,
  mediaType: string,
  searchTerm: string,
  pageNumber = 0,
  pageSize = 10,
) {
  const url = `${contextPath}api/${typeToEndpointMapping.fileResource}/type/${mediaType}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, totalElements} = json
    return {
      content,
      totalElements,
    }
  } catch (err) {
    return {
      content: [],
      totalElements: 0,
    }
  }
}

export async function updateAttachedIdentifiablesOrder(
  contextPath: string,
  identifiables: any[],
  parentType: string,
  parentUuid: string,
  type: string,
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

export async function updateFileResource(
  contextPath: string,
  fileResource: any,
) {
  return await updateIdentifiable(contextPath, fileResource, 'fileResource')
}

export async function updateIdentifiable(
  contextPath: string,
  identifiable: any,
  type: string,
) {
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

export async function uploadFile(
  contextPath: string,
  file: any,
  updateProgress: (progress: number) => void,
) {
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

    request.open(
      'POST',
      `${contextPath}api/${typeToEndpointMapping.file}`,
      true,
    )
    const formData = new FormData()
    formData.append('userfile', file, file.name)
    request.send(formData)
  })
}
