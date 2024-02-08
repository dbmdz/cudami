interface Order {
  property: string
  subProperty?: string
}

interface Sorting {
  orders: Order[]
}

interface ListRequest {
  searchTerm?: string
  sorting?: Sorting
}

interface PageRequest extends ListRequest {
  pageNumber: number
  pageSize: number
}

const convertOrdersToString = (orders: Order[]): string =>
  orders
    .map(({property, subProperty}) =>
      subProperty ? `${property}_${subProperty}` : property,
    )
    .join(',')

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
  item: 'items',
  language: 'languages',
  license: 'licenses',
  person: 'persons',
  project: 'projects',
  renderingTemplate: 'renderingtemplates',
  topic: 'topics',
  urlAlias: 'urlaliases',
  user: 'users',
  webpage: 'webpages',
  website: 'websites',
  work: 'works',
}

export async function addAttachedObject(
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

export async function addAttachedObjects(
  contextPath: string,
  objects: any[],
  parentType: string,
  parentUuid: string,
  type: string,
) {
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(objects),
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

export async function findAttachedObjects(
  contextPath: string,
  parentType: string,
  parentUuid: string,
  type: string,
  {pageNumber, pageSize, searchTerm}: PageRequest,
) {
  let url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = `${url}&searchTerm=${encodeURIComponent(searchTerm)}`
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

export async function findRootObjects(
  contextPath: string,
  type: string,
  {pageNumber, pageSize, searchTerm, sorting}: PageRequest,
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}?pageNumber=${pageNumber}&pageSize=${pageSize}`
  if (searchTerm) {
    url = `${url}&searchTerm=${encodeURIComponent(searchTerm)}`
  }
  if (sorting?.orders) {
    url = `${url}&sortBy=${convertOrdersToString(sorting.orders)}`
  }
  try {
    const response = await fetch(url)
    const json = await response.json()
    const {content, pageRequest, rows, total, totalElements} = json
    return {
      content: rows ?? content,
      pageSize: pageRequest?.pageSize,
      totalElements: total ?? totalElements,
    }
  } catch (err) {
    return {
      content: [],
      pageSize: 0,
      totalElements: 0,
    }
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

export async function getAvailableLanguages(
  contextPath: string,
): Promise<string[]> {
  const url = `${contextPath}api/${typeToEndpointMapping.language}`
  try {
    const response = await fetch(url)
    return await response.json()
  } catch (err) {
    return []
  }
}

export async function getByIdentifier(
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

export async function getByUuid(
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

export async function getDefaultLanguage(contextPath: string): Promise<string> {
  const url = `${contextPath}api/${typeToEndpointMapping.language}/default`
  try {
    const response = await fetch(url)
    return await response.json()
  } catch (err) {
    return 'en'
  }
}

export async function getUser(
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

export async function removeAttachedObject(
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

export async function save(
  contextPath: string,
  object: any,
  type: string,
  {parentType, parentUuid}: Record<string, string> = {},
) {
  let url = `${contextPath}api/${typeToEndpointMapping[type]}`
  if (parentType && parentUuid) {
    url = `${url}?parentType=${parentType}&parentUuid=${parentUuid}`
  }
  try {
    const response = await fetch(url, {
      body: JSON.stringify(object),
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

export async function search(
  contextPath: string,
  type: string,
  {pageNumber, pageSize, searchTerm, sorting}: PageRequest,
) {
  let url = `${contextPath}api/${
    typeToEndpointMapping[type]
  }/search?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${encodeURIComponent(
    searchTerm ?? '',
  )}`
  if (sorting?.orders) {
    url = `${url}&sortBy=${convertOrdersToString(sorting.orders)}`
  }
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
  {pageNumber, pageSize, searchTerm, sorting}: PageRequest,
) {
  let url = `${contextPath}api/${
    typeToEndpointMapping.fileResource
  }/type/${mediaType}?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${encodeURIComponent(
    searchTerm ?? '',
  )}`
  if (sorting?.orders) {
    url = `${url}&sortBy=${convertOrdersToString(sorting.orders)}`
  }
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

export async function update(contextPath: string, object: any, type: string) {
  const url = `${contextPath}api/${typeToEndpointMapping[type]}/${object.uuid}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(object),
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

export async function updateAttachedObjectsOrder(
  contextPath: string,
  objects: any[],
  parentType: string,
  parentUuid: string,
  type: string,
) {
  const url = `${contextPath}api/${typeToEndpointMapping[parentType]}/${parentUuid}/${typeToEndpointMapping[type]}`
  try {
    const response = await fetch(url, {
      body: JSON.stringify(objects),
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
