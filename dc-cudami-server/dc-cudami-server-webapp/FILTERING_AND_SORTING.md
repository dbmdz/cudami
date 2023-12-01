Filtering and Sorting How-To
============================

Filtering
---------

### General usage

For filtering *one* `filtering` request parameter is used, e.g.
```
…/v6/digitalobjects?filtering=…
```
This parameter uses a quite complex syntax but simplifications for not highly sophisticated criteria were added.
Use the `filtering` parameter as follows:

- single conditions are separated by semicolon  
  > `?filtering=lastname:eq:foo;age:gt:30` → lastname == "foo" AND age > 30
- …and can be surrounded by braces (we call them "FilterCriteria")  
  > `?filtering={lastname:eq:foo;age:gt:30}` → same
- `$AND` and `$OR` determines how all of the conditions within braces are logically linked
- by default the FilterCriteria are linked by *AND*, so `$AND` can be omitted  
  > `?filtering={lastname:eq:foo;age:gt:30}` is same as `?filtering={$AND;lastname:eq:foo;age:gt:30}`
- several FilterCriteria can be appended, seperated by semicolon; these FilterCriteria are always linked by *AND*  
  > `?filtering={$OR;lastname:eq:foo;firstname:eq:foo};{age:gt:30}` → (lastname == "foo" OR firstname == "foo") AND age > 30  
  > `?filtering={$OR;lastname:eq:foo;firstname:eq:foo};{$OR;age:gt:30;age:lt:20}` → (lastname == "foo" OR firstname == "foo") AND (age > 30 OR age < 20)

### Important notes

- within the value(s) of a condition semicolons and braces (i.e. `;{}`) must be backslash escaped
  (not when using the cudami client)
- URL encoding is necessary as usual (cudami client does it for you already)

### A word about Regular Expressions

> …that must be obeyed with the cudami client too

- The operators `regex`, `iregex` and their negative counterparts `nregex` & `niregex`
  use the regular expression syntax of the underlying database, i.e. PostgreSQL
  ([see here](https://www.postgresql.org/docs/13/functions-matching.html#FUNCTIONS-POSIX-REGEXP)).
- The quantifier `+` **cannot be used** because spring boot always (even URL encoded) replaces it by a space;
  please use `{1,}` (in the URL `\{1,\}`) instead. The same issues occur with its non-greedy variant
  `+?` that must be replaced by `{1,}?` (URL: `\{1,\}?`).
- Regular expressions are not anchored by default thus they may take a long execution time in large tables.
  So whenever possible use anchors (`^` or `$`) and avoid unbound quantifiers (e.g. `*`, `{1,}`).
  This does not only make a small difference, it increases speed up to 95 percent.

Sorting
-------

For sorting use the `sortBy` request parameter. It takes a comma separated list of "orderings";
you may also specify this parameter more than once.  

An "ordering" has this syntax:

- the name of the property, optionally followed by an underscore and the subproperty, e.g.  
  > `?sortBy=lastModified&sortBy=label_de`
- the following dot separated qualifiers, each may be omitted but do not change the order if using more than one of them

  * direction of sorting: `asc` or `desc`  
    > `?sortBy=lastModified.desc`
  * null handling: `nullsfirst` or `nullslast`  
    > `?sortBy=label_en.nullslast`
  * case sensitivity: `ignorecase`  
    > `?sortBy=label_de.ignorecase`

  some examples with all of them:  
  > `?sortBy=lastModified.desc,label_en.asc.nullslast.ignorecase`  
  > `?sortBy=name.asc.ignorecase`

### Important notes

Please keep in mind that sorting is a really "expensive" execution step inside the database.
So whenever possible try to avoid it and prefer to sort the response in your consumer app.
For paged responses this might seem to be cumbersome but it increases the speed of the database, thus reduces the time of the response.
Of course it always depends on the specific case so please try out what works best with regard to the request-response time.

