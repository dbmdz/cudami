export function getAvailableLocales () {
  return ['es', 'fr'];
}

export async function loadAvailableLocales () {
  try {
    const result = await fetch('/api/locales');
    return result.json();
  } catch(err) {
    return [];
  }
};

export async function loadIdentifiable (type, uuid) {
  const url = uuid === 'mock' ? `__mock__/${type}.json` : `/api/${type}s/${uuid}`;
  try {
    const result = await fetch(url);
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
