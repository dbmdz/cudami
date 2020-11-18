# `cudami` editor component

## Local development without java backend

see [official documentation](https://webpack.js.org/configuration/dev-server/)

1. install dependencies: `npm install`
2. start webpack development server: `npm run start`
3. open <http://localhost:3000> to browse to the development UI implemented in `src/App.jsx`:

![Development UI](./assets/development-ui.png)

Starting from this development-only startpage all subsequent pages are productive code.

*Recommended for inspecting state and props:* React developer tools for [Chrome](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi) and [Firefox](https://addons.mozilla.org/en-US/firefox/addon/react-devtools/)

After changing code in editor component, compile the whole application:

```
$ cd ..
$ mvn clean install -U
```

Start application (repository server and management webapp) and test your changes.
