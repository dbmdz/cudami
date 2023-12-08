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

### Fields with subproperties

Generally speaking subproperties are separated from the field name by a dot (`.`). That is the only general rule because
filtering by a field within another field is always a special case and thus a special implementation.
Please check out the methods `getColumnName` of the desired object's repository implementation.

The specials that are shared by all `Identifiable`s are:

- label filtering:

  - for the sake of performance use `like` operator
  - filtering with `like` searches for the occurrence of the single words (whole words) w/o considering their order
    or any intermediate words (you might filter the result in your consumer app by your needs)
  - filtering with `eq` (**not recommended**) is much slower and searches for an exact match of the supplied string
    within the label (i.e. somewhere in the label string, not anchored)
  - `label` ignores the language and is fastest
  - `label.<language>`, e.g. `label.de`, `label.und-Latn` filters by language too but is slower

- for `NamedEntity`s only: name filtering:

  - field is `name` or `name.<language>`
  - the rules and hints for label filtering (see above) apply analogously

- `identifiers.namespace` and/or `identifiers.id` (both are string fields)

### List of available filter operators

<table border="1">
<caption>Filter operators for URL query params</caption>
<thead>
<tr><th>Symbol    </th><th>Operation                                 </th><th>Example filter query param (unescaped) </th></tr>
</thead>
<tbody>
<tr><td>eq        </td><td>equals                                    </td><td>city:eq:Munich                         </td></tr>
<tr><td>eq_notset </td><td>equals or not set                         </td><td>city:eq_notset:Munich                  </td></tr>
<tr><td>neq       </td><td>not equals                                </td><td>country:neq:de                         </td></tr>
<tr><td>gt        </td><td>greater than                              </td><td>amount:gt:10000                        </td></tr>
<tr><td>gt_notset </td><td>greater than or not set                   </td><td>presentationEnd:gt_notset:2020-10-06   </td></tr>
<tr><td>gte       </td><td>greater than or equals                    </td><td>amount:gte:10000                       </td></tr>
<tr><td>gte_notset</td><td>greater than or equals or not set         </td><td>amount:gte_notset:10000                </td></tr>
<tr><td>lt        </td><td>less than                                 </td><td>amount:lt:10000                        </td></tr>
<tr><td>lt_notset </td><td>less than or not set                      </td><td>amount:lt_notset:10000                 </td></tr>
<tr><td>lt_set    </td><td>less than and set                         </td><td>amount:lt_set:10000                    </td></tr>
<tr><td>lte       </td><td>less than or equals to                    </td><td>amount:lte:10000                       </td></tr>
<tr><td>lte_set   </td><td>less than or equals and set               </td><td>presentationStart:lte_set:2020-10-06   </td></tr>
<tr><td>lte_notset</td><td>less than or equals or not set            </td><td>presentationStart:lte_notset:2020-10-06</td></tr>
<tr><td>in        </td><td>in                                        </td><td>country:in:uk,usa,au                   </td></tr>
<tr><td>nin       </td><td>not in                                    </td><td>country:nin:fr,de,nz                   </td></tr>
<tr><td>btn       </td><td>between (inclusive)                       </td><td>joiningDate:btn:2018-01-01,2016-01-01  </td></tr>
<tr><td>like      </td><td>like                                      </td><td>firstName:like:John                    </td></tr>
<tr><td>stw       </td><td>starts with                               </td><td>firstName:stw:A                        </td></tr>
<tr><td>set       </td><td>value exists (not null)                   </td><td>firstName:set:                         </td></tr>
<tr><td>notset    </td><td>value is not set (null)                   </td><td>firstName:notset:                      </td></tr>
<tr><td>regex     </td><td>regexp matching (case sensitive)          </td><td>firstName:regex:Joh?n.\{1,\}           </td></tr>
<tr><td>iregex    </td><td>regexp matching (case insensitive)        </td><td>firstName:iregex:Joh?n.\{1,\}          </td></tr>
<tr><td>nregex    </td><td>regexp not matching (case sensitive)      </td><td>firstName:nregex:Joh?n.\{1,\}          </td></tr>
<tr><td>niregex   </td><td>regexp not matching (case insensitive)    </td><td>firstName:niregex:Joh?n.\{1,\}         </td></tr>
</tbody>
</table>

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

