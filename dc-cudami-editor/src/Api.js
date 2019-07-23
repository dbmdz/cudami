export async function loadAvailableLocales () {
  return ['es', 'fr'];
};

export async function loadIdentifiable (type, uuid) {
  return {
    "created": "2018-08-21T16:22:52.033",
    "description": {
      "en": {
        "type": "doc",
        "content": [
          {
            "type": "paragraph",
            "content": [
              {
                "type": "text",
                "text": "Englische Beschreibung"
              }
            ]
          }
        ]
      }
    },
    "label": {
      "de_DE": "Deutscher Titel",
      "en": "Englischer Titel"
    },
    "lastModified": "2018-08-21T16:22:52.033",
    "text": {
      "de_DE": {
        "type": "doc",
        "content": [
          {
            "type": "paragraph",
            "content": [
              {
                "type": "text",
                "text": "Deutscher Text"
              }
            ]
          }
        ]
      }
    },
    "type": "ENTITY",
    "uuid": "546aac6c-ee00-41b9-a47e-9673a398f171",
    "entityType": "WEBSITE",
    "rootPages": [],
    "url": "http://bsb.de"
  };
  /*try {
    const result = await fetch(`/latest/${type}/${uuid}.json`);
    return result.json();
  } catch(err) {
    return {};
  }*/
};
