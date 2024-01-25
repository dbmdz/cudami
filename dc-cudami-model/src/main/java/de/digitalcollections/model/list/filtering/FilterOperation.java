package de.digitalcollections.model.list.filtering;

/**
 * Available filter operations
 *
 * <table border="1">
 * <caption>Mapping operation abbreviation to filter operation</caption>
 * <tr><th>Symbol    </th><th>Operation                        </th><th>Example filter query param (unescaped) </th></tr>
 * <tr><td>eq        </td><td>equals                           </td><td>city:eq:Munich                         </td></tr>
 * <tr><td>eq_notset </td><td>equals or not set                </td><td>city:eq_notset:Munich                  </td></tr>
 * <tr><td>neq       </td><td>not equals                       </td><td>country:neq:de                         </td></tr>
 * <tr><td>gt        </td><td>greater than                     </td><td>amount:gt:10000                        </td></tr>
 * <tr><td>gt_notset </td><td>greater than or not set          </td><td>presentationEnd:gt_notset:2020-10-06   </td></tr>
 * <tr><td>gte       </td><td>greater than or equals           </td><td>amount:gte:10000                       </td></tr>
 * <tr><td>gte_notset</td><td>greater than or equals or not set</td><td>amount:gte_notset:10000                </td></tr>
 * <tr><td>lt        </td><td>less than                        </td><td>amount:lt:10000                        </td></tr>
 * <tr><td>lt_notset </td><td>less than or not set             </td><td>amount:lt_notset:10000                 </td></tr>
 * <tr><td>lt_set    </td><td>less than and set                </td><td>amount:lt_set:10000                    </td></tr>
 * <tr><td>lte       </td><td>less than or equals to           </td><td>amount:lte:10000                       </td></tr>
 * <tr><td>lte_set   </td><td>less than or equals and set      </td><td>presentationStart:lte_set:2020-10-06   </td></tr>
 * <tr><td>lte_notset</td><td>less than or equals or not set   </td><td>presentationStart:lte_notset:2020-10-06</td></tr>
 * <tr><td>in        </td><td>in                               </td><td>country:in:uk,usa,au                   </td></tr>
 * <tr><td>nin       </td><td>not in                           </td><td>country:nin:fr,de,nz                   </td></tr>
 * <tr><td>btn       </td><td>between (inclusive)              </td><td>joiningDate:btn:2018-01-01,2016-01-01  </td></tr>
 * <tr><td>like      </td><td>like                             </td><td>firstName:like:John                    </td></tr>
 * <tr><td>stw       </td><td>starts with                      </td><td>firstName:stw:A                        </td></tr>
 * <tr><td>set       </td><td>value exists (not null)          </td><td>firstName:set:                         </td></tr>
 * <tr><td>notset    </td><td>value is not set (null)          </td><td>firstName:notset:                      </td></tr>
 * <tr><td>regex     </td><td>regexp matching (case sensitive) </td><td>firstName:regex:Joh?n.+                </td></tr>
 * <tr><td>iregex    </td><td>regexp matching (case insensitive)</td><td>firstName:iregex:Joh?n.+              </td></tr>
 * <tr><td>nregex    </td><td>regexp not matching (case sensitive) </td><td>firstName:nregex:Joh?n.+           </td></tr>
 * <tr><td>niregex   </td><td>regexp not matching (case insensitive)</td><td>firstName:niregex:Joh?n.+         </td></tr>
 * </table>
 *
 * <p>References:
 *
 * @see <a href="https://github.com/vijjayy81/spring-boot-jpa-rest-demo-filter-paging-sorting">An
 *     example application using Spring boot MVC, Spring Data JPA with the ability to do filter,
 *     pagination and sorting.</a>
 */
public enum FilterOperation {
  EQUALS("eq", OperandCount.SINGLEVALUE),
  EQUALS_OR_NOT_SET("eq_notset", OperandCount.SINGLEVALUE),
  NOT_EQUALS("neq", OperandCount.SINGLEVALUE),
  GREATER_THAN("gt", OperandCount.SINGLEVALUE),
  GREATER_THAN_OR_NOT_SET("gt_notset", OperandCount.SINGLEVALUE),
  GREATER_THAN_OR_EQUAL_TO("gte", OperandCount.SINGLEVALUE),
  GREATER_THAN_OR_EQUAL_TO_OR_NOT_SET("gte_notset", OperandCount.SINGLEVALUE),
  LESS_THAN("lt", OperandCount.SINGLEVALUE),
  LESS_THAN_AND_SET("lt_set", OperandCount.SINGLEVALUE),
  LESS_THAN_OR_NOT_SET("lt_notset", OperandCount.SINGLEVALUE),
  LESS_THAN_OR_EQUAL_TO("lte", OperandCount.SINGLEVALUE),
  LESS_THAN_OR_EQUAL_TO_AND_SET("lte_set", OperandCount.SINGLEVALUE),
  LESS_THAN_OR_EQUAL_TO_OR_NOT_SET("lte_notset", OperandCount.SINGLEVALUE),
  IN("in", OperandCount.MULTIVALUE),
  NOT_IN("nin", OperandCount.MULTIVALUE),
  BETWEEN("btn", OperandCount.MIN_MAX_VALUES),
  CONTAINS("like", OperandCount.SINGLEVALUE),
  NOT_SET("notset", OperandCount.NO_VALUE),
  SET("set", OperandCount.NO_VALUE),
  STARTS_WITH("stw", OperandCount.SINGLEVALUE),
  REGEX("regex", OperandCount.SINGLEVALUE),
  IREGEX("iregex", OperandCount.SINGLEVALUE),
  NOT_REGEX("nregex", OperandCount.SINGLEVALUE),
  NOT_IREGEX("niregex", OperandCount.SINGLEVALUE);

  public static FilterOperation fromValue(String value) {
    for (FilterOperation op : FilterOperation.values()) {

      // Case insensitive operation name
      if (String.valueOf(op.value).equalsIgnoreCase(value)) {
        return op;
      }
    }
    return null;
  }

  private final OperandCount opCount;

  private final String value;

  FilterOperation(String value, OperandCount opCount) {
    this.value = value;
    this.opCount = opCount;
  }

  public OperandCount getOperandCount() {
    return opCount;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public enum OperandCount {
    SINGLEVALUE,
    MIN_MAX_VALUES,
    MULTIVALUE,
    NO_VALUE;
  }
}
