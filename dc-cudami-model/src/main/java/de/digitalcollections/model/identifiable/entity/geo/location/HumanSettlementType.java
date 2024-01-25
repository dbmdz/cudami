package de.digitalcollections.model.identifiable.entity.geo.location;

/**
 * Types of human settlements:
 *
 * <ul>
 *   <li>village: small clustered human settlement smaller than a town
 *       (https://www.wikidata.org/wiki/Q532)
 *   <li>town: settlement that is bigger than a village but smaller than a city
 *       (https://www.wikidata.org/wiki/Q3957)
 *   <li>city: large and permanent human settlement (https://www.wikidata.org/wiki/Q515)
 *   <li>city/town: human settlement ("Stadtsiedlung") (https://www.wikidata.org/wiki/Q7930989)
 * </ul>
 */
public enum HumanSettlementType {
  VILLAGE,
  TOWN,
  CITY,
  CITY_OR_TOWN;
}
