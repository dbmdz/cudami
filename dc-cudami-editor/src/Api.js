export async function loadAvailableLocales () {
  try {
    const result = await fetch('/api/locales');
    return result.json();
  } catch(err) {
    return [];
  }
};

export async function loadIdentifiable (type, uuid) {
  try {
    const result = await fetch(`/api/${type}s/${uuid}`);
    return result.json();
  } catch(err) {
    return {};
  }
};

export async function saveIdentifiable (identifiable, type) {

};

export async function updateIdentifiable (identifiable, type, uuid) {
  await fetch(`/api/${type}s/${uuid}`, {
    body: JSON.stringify(identifiable),
    headers: {
      'Content-Type': 'application/json',
      credentials: 'same-origin'
    },
    method: 'PUT',
  }).then(response => {
    if (response.redirected) {
      window.location.href = response.url;
    }
  }).catch(err => {
    console.log('An error occured');
  });
};
