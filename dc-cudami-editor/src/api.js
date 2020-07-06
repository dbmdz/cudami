import React from 'react'

export const ApiContext = React.createContext(null)

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
  let url = `${contextPath}api/${type.toLowerCase()}s/${uuid}`
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
  let url = `${contextPath}api/${type.toLowerCase()}s/new`
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
      const viewUrl = `${contextPath}${type.toLowerCase()}s/${json.uuid}`
      window.location.href = viewUrl
    } else {
      return json
    }
  } catch (err) {
    console.log('An error occured')
  }
}

export async function searchImages(
  contextPath,
  mock,
  searchTerm,
  pageNumber = 0,
  pageSize = 10
) {
  let url = `${contextPath}api/fileresources/images?pageNumber=${pageNumber}&pageSize=${pageSize}&searchTerm=${searchTerm}`
  if (mock) {
    url = '/__mock__/images.json'
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
  const url = `${contextPath}api/${type.toLowerCase()}s/${identifiable.uuid}`
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
      const viewUrl = `${contextPath}${type.toLowerCase()}s/${json.uuid}`
      window.location.href = viewUrl
    } else {
      return json
    }
  } catch (err) {
    console.log('An error occured')
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
    request.onerror = () => reject(request.statusText)
    request.onload = () => {
      if (request.status >= 200 && request.status < 300) {
        resolve(JSON.parse(request.response))
      } else {
        reject(request.statusText)
      }
    }
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
