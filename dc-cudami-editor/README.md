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

## Format Javascript Files

Install prettier:

```
$ npm install --save-dev --save-exact prettier
```

Format files:

```
$ npx prettier --write "src/**/*.js"
$ npx prettier --write "src/**/*.jsx"
```

or

```
npm install --only=dev && npm run format-check
```

## Typical list of files to add/change for a new object type

Example: persons list

* cudami/dc-cudami-admin/src/main/resources/templates/main.html: add icon with link to list

```
<div class="card">
  <a class="btn" th:href="@{/persons}" th:title="#{persons}">
    <div class="card-body">
      <div class="icon"><i class="fas fa-users"></i></div>
    </div>
    <div class="card-footer"th:text="#{persons}">Persons</div>
  </a>
</div>
```

* cudami/dc-cudami-admin/src/main/java/de/digitalcollections/cudami/admin/controller/identifiable/entity/agent/PersonsController.java

```
@GetMapping("/api/persons")
@ResponseBody
public PageResponse<PersonImpl> findAll(
    @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
    @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize)
    throws HttpException {
  List<Order> orders = new ArrayList<>();
  OrderImpl labelOrder = new OrderImpl("label");
  labelOrder.setSubProperty(localeService.getDefaultLanguage().getLanguage());
  orders.addAll(Arrays.asList(labelOrder));
  Sorting sorting = new SortingImpl(orders);
  PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
  return service.find(pageRequest);
}
```

* cudami/dc-cudami-admin/src/main/resources/templates/persons/list.html:

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="https://github.com/thymeleaf/thymeleaf-extras-springsecurity"
      xmlns:data="https://github.com/mxab/thymeleaf-extras-data-attribute"
      layout:decorate="~{base}">
  <head>
    <title th:text="#{persons}">Persons</title>
  </head>
  <body>
    <section layout:fragment="content">
      <div id="list"></div>
      <script th:src="@{/js/persons-list.bundle.js}"></script>
      <script th:inline="javascript">
        PersonsList({
          apiContextPath: /*[[@{/}]]*/ '',
          id: "list",
          uiLocale: /*[[${#locale.language}]]*/
        });
      </script>
    </section>
  </body>
</html>
```

* cudami/dc-cudami-editor/src/lib/PersonsList.jsx:

```
import React from 'react'
import ReactDOM from 'react-dom'

import initI18n from '../i18n'
import PagedPersonsList from '../components/PagedPersonsList'

export default function ({apiContextPath, id, uiLocale}) {
  initI18n(uiLocale)
  ReactDOM.render(
    <PagedPersonsList apiContextPath={apiContextPath} />,
    document.getElementById(id)
  )
}
```

* cudami/dc-cudami-editor/src/components/PagedPersonsList.jsx

```
import React, {useEffect, useState} from 'react'
import {Button, Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import LanguageTab from './LanguageTab'
import ListButtons from './ListButtons'
import ListPagination from './ListPagination'
import {loadDefaultLanguage, typeToEndpointMapping} from '../api'
import usePagination from '../hooks/usePagination'

const PagedPersonsList = ({
  apiContextPath = '/',
  mockApi = false,
}) => {
  const type = 'person'
  const {
    content: persons,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  } = usePagination(apiContextPath, mockApi, type)
  const [defaultLanguage, setDefaultLanguage] = useState('')
  useEffect(() => {
    loadDefaultLanguage(apiContextPath, mockApi).then((defaultLanguage) =>
      setDefaultLanguage(defaultLanguage)
    )
  }, [])
  const {t} = useTranslation()
  return (
    <>
      <Row>
        <Col>
          <h1>{t('persons')}</h1>
        </Col>
        <Col className="text-right">
        {/*
          <Button href={`${apiContextPath}${typeToEndpointMapping[type]}/new`}>
            {t('new')}
          </Button>
        */}
        </Col>
      </Row>
      <Row>
        <Col>
          <hr />
        </Col>
      </Row>
      <Nav tabs>
        <LanguageTab
          activeLanguage={defaultLanguage}
          language={defaultLanguage}
          toggle={() => {}}
        />
      </Nav>
      <Card className="border-top-0">
        <CardBody>
          <ListPagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            totalElements={totalElements}
            type={type}
          />
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <thead>
              <tr>
                <th className="text-center">{t('label')}</th>
                <th className="text-center">{t('description')}</th>
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {persons.map(({description, label, name, uuid}) => (
                <tr key={uuid}>
                  <td>{label?.[defaultLanguage]}</td>
                  <td>{description?.[defaultLanguage]}</td>
                  <td className="text-center">
                    <ListButtons
                      editUrl={`${apiContextPath}${typeToEndpointMapping[type]}/${uuid}/edit`}
                      showEdit={false}
                      showView={false}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <ListPagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            position="under"
            showTotalElements={false}
            totalElements={totalElements}
            type={type}
          />
        </CardBody>
      </Card>
    </>
  )
}

export default PagedPersonsList
```

* cudami/dc-cudami-editor/webpack.config.js:

```
const config = {
  cache: true,
  devtool: 'sourcemaps',
  entry: {
    IdentifiableEditor: './src/lib/IdentifiableEditor.jsx',
    IdentifiableList: './src/lib/IdentifiableList.jsx',
    **PersonsList: './src/lib/PersonsList.jsx',**
    RenderingTemplateEditor: './src/lib/RenderingTemplateEditor.jsx',
    RenderingTemplateList: './src/lib/RenderingTemplateList.jsx'
  },
```

* cudami/dc-cudami-editor/src/locales/de/translation.json and
  cudami/dc-cudami-editor/src/locales/en/translation.json

```
    "person": "Person",
    "persons": "Persons",

"totalElements": {
      "digitalObjects": "found {{ count }} digital object",
      "digitalObjects_plural": "found {{ count }} digital objects",
      **"persons": "found {{ count }} person",**
      **"persons_plural": "found {{ count }} persons",**
```

* cudami/dc-cudami-editor/src/api.js

```
export const typeToEndpointMapping = {
  article: 'articles',
  collection: 'collections',
  corporateBody: 'corporatebodies',
  digitalObject: 'digitalobjects',
  fileResource: 'fileresources',
  **person: 'persons',**
```

* 