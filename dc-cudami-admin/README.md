# cudami Management Webapp

This is the GUI for administrating (content and users in) cudami.

## Usage

Start webapp JAR by specifiying cudami backend rest url using params:

Example:

``` sh
java -jar dc-cudami-admin-webapp-7.0.0-SNAPSHOT.jar --cudami.server.address=<your_endpoint_address> --cudami.server.url=<your_endpoint_url>
```

## Frameworks

### Javascript and CSS

* AdminLTE 3.2.0
* Bootstrap v4.6.1 (https://getbootstrap.com/)
* Bootstrap-Table 1.21.2
* filesize 9.0.1
* Font Awesome Free 5.15.4
* jQuery 3.6.0
* jQuery-Autocomplete tomik23/autocomplete 1.8.6
* TableDnD 1.0.4 (http://isocra.github.io/TableDnD/)
* TipTap 2.0.0beta

## Migrations

### Migration to Thymeleaf 3

References:

* http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html
* https://ultraq.github.io/thymeleaf-layout-dialect/MigrationGuide.html
* https://github.com/thymeleaf/thymeleaf/issues/451

### Migration to VUE.js 3

Homepage: <https://vuejs.org/>

1. Add webjars in `pom.xml`:

```
<dependency>
  <groupId>org.webjars.npm</groupId>
  <artifactId>axios</artifactId>
  <version>1.3.6</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.webjars.npm</groupId>
  <artifactId>github-com-Templarian-MaterialDesign-Webfont</artifactId>
  <version>3.6.95</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.webjars.npm</groupId>
  <artifactId>vue</artifactId>
  <version>3.2.38</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.webjars.npm</groupId>
  <artifactId>vuetify</artifactId>
  <version>3.0.6</version>
  <scope>runtime</scope>
</dependency>
```

2. Embed to test page:

```
<head>
  <link th:href="@{/webjars/github-com-Templarian-MaterialDesign-Webfont/materialdesignicons.min.css}" rel="stylesheet">
  <link th:href="@{/webjars/vuetify/dist/vuetify.min.css}" rel="stylesheet">
  <script th:src="@{/webjars/axios/dist/axios.min.js}"></script>
  <script th:src="@{/webjars/vue/dist/vue.global.js}"></script>
  <script th:src="@{/webjars/vuetify/dist/vuetify.min.js}"></script>
</head>
```

3. Use VUE:

HTML (see <https://vuejs.org/guide/essentials/list.html>):

```
<div id="data-table-users">
  <table class="table table-striped table-bordered">
    <thead>
      <tr>
        <th th:text="#{lbl.id}">ID</th>
        <th th:text="#{lbl.firstname}">Firstname</th>
        <th th:text="#{lbl.lastname}">Lastname</th>
        <th th:text="#{lbl.email}">Email</th>
        <th th:text="#{lbl.roles}">Role(s)</th>
        <th th:text="#{lbl.actions}">Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="user in users">
        <td>{{ user.id }}</td>
        <td>{{ user.firstname }}</td>
        <td>{{ user.lastname }}</td>
        <td>{{ user.email }}</td>
        <td>{{ user.roles }}</td>
        <td>
          <a>Edit</a>
          <a v-bind:href="'/users/delete/' + user.id">Delete</a>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

Javascript:

```
<th:block layout:fragment="beforeBodyEnds">
  <script th:inline="javascript">
    const {createApp} = Vue;
    const {createVuetify} = Vuetify;
    const vuetify = createVuetify();

    const app = createApp({
      data() {
        return {
          users: [],
        }
      },
      mounted() {
        // mounted() is called when Vue.js is ready (mounted on the element specified in el above)
        let url = /*[[@{/api/v1/users}]]*/ '';
        axios
          .get(url)
          .then(response => (this.users = response.data.users))
      },
    });
    app.use(vuetify).mount('#data-table-users');
  </script>
</th:block>
```

Java:

```
@GetMapping("/api/v1/users")
public ResponseEntity<Map<String, Object>> findAll(
    @RequestParam(required = false) String email,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size)
    throws ServiceException {
  try {
    PageRequest pageRequest = PageRequest.builder().pageNumber(page).pageSize(size).build();
    PageResponse<User> pageResponse = service.find(pageRequest);

    List<User> users = pageResponse.getContent();

    Map<String, Object> response = new HashMap<>(4);
    response.put("users", users);
    response.put("currentPage", pageResponse.getPageNumber());
    response.put("totalItems", pageResponse.getTotalElements());
    response.put("totalPages", pageResponse.getTotalPages());
    return new ResponseEntity<>(response, HttpStatus.OK);
  } catch (Exception e) {
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
```
