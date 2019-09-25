export function getAvailableLanguages () {
  return ['es', 'fr'];
}

export async function loadAvailableLanguages (contextPath) {
  const url = `${contextPath}api/languages`;
  try {
    const result = await fetch(url);
    return result.json();
  } catch(err) {
    return [];
  }
};

export async function loadIdentifiable (contextPath, type, uuid) {
  const url = uuid === 'mock' ? `__mock__/${type}.json` : `${contextPath}api/${type.toLowerCase()}s/${uuid}`;
  try {
    const result = await fetch(url);
    return result.json();
  } catch(err) {
    return {};
  }
};

export async function saveIdentifiable (contextPath, identifiable, parentType, parentUuid, type) {
  let url = `${contextPath}api/${type.toLowerCase()}s/new`;
  if (parentType && parentUuid) {
    url = `${url}?parentType=${parentType}&parentUuid=${parentUuid}`
  }
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin'
      },
      method: 'POST',
    });
    if (response.redirected) {
      window.location.href = response.url;
    }
  } catch(err) {
    console.log('An error occured');
  }
};

export async function updateIdentifiable (contextPath, identifiable, type) {
  const url = `${contextPath}api/${type.toLowerCase()}s/${identifiable.uuid}`;
  try {
    const response = await fetch(url, {
      body: JSON.stringify(identifiable),
      headers: {
        'Content-Type': 'application/json',
        credentials: 'same-origin'
      },
      method: 'PUT',
    });
    if (response.redirected) {
      window.location.href = response.url;
    }
  } catch(err) {
    console.log('An error occured');
  }
};

export async function uploadFile (contextPath, file, type, updateProgress) {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.onerror = () => reject(request.statusText);
    request.onload = () => {
      if (request.status >= 200 && request.status < 300) {
        resolve(request.response);
      } else {
        reject(request.statusText);
      }
    }
    request.upload.addEventListener('progress', evt => {
      if (evt.lengthComputable) {
        updateProgress(Math.round((evt.loaded / evt.total) * 100));
      }
    });
    request.open('POST', `${contextPath}api/${type.toLowerCase()}s/new/upload`, true);
    const formData = new FormData();
    formData.append('userfile', file, file.name);
    request.send(formData);
  });
};
