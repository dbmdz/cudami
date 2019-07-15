export async function loadIdentifiable (baseUrl, type, uuid) {
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
      "translations": [
        {
          "locale": "de_DE",
          "text": "Deutscher Titel"
        },
        {
          "locale": "en",
          "text": "Englischer Titel"
        }
      ]
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
    const result = await fetch(`${baseUrl}/latest/${type}/${uuid}.json`);
    return result.json();
  } catch(err) {
    return {};
  }*/
}
