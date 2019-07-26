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
