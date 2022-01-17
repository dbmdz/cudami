# cudami Website Template #1

This webapp is an out of the box cudami Website "viewer".
It provides the web-frontend (HTML) for a website and its webpages created in cudami.

All you need to do is
- create a website (in cudami Management webapp)
- create webpages under this website (in cudami Management webapp)

Then copy the UUID of the created website and start this webapp
- giving the UUID of the website and
- the server url of the cudami backend (API) (without trailing slash)
as params like this, e.g.:

```
java -jar template-website-sidebar-nav-0.0.1-SNAPSHOT.jar \
  -Dcudami.server.url=http://localhost:9000 \
  -Dcudami.website=ea9ddc66-e822-4867-9585-a43c6ed8bd98
```

Open your browser and reqeust `http://localhost:8080`.

The website will be rendered with all webpages (down to given level, default = 3) as sidebar navigation.


 -Dtemplate.navMaxLevel=1

java -jar template-website-sidebar-nav-0.0.1-SNAPSHOT.jar -Dcudami.server.url=http://localhost:9000 -Dcudami.website=ea9ddc66-e822-4867-9585-a43c6ed8bd98 -Dcudami.webpages.content=ead664b6-5fcc-414e-b3bb-133f0af1acb8 -Dcudami.webpages.footer=6bcce154-e216-4223-a4f7-d9aa99d42695