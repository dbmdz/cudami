package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;

public interface GeoLocationService<G extends GeoLocation> extends EntityService<G> {

  static final double EARTH_RADIUS_KM = 6371.009; // [km] 6378.137

  /**
   * Converts from degrees to decimal degrees
   *
   * @param degree degrees
   * @param minutes minutes
   * @param seconds seconds
   * @return decimal degrees
   */
  static double deg2dec(final int degree, final int minutes, final int seconds) {
    final double dec = degree + ((double) minutes / 60) + ((double) seconds / (60 * 60));
    return dec;
  }

  /**
   * Converts decimal degrees to radians
   *
   * @param deg decimal degrees
   * @return radians
   */
  static double deg2rad(final double deg) {
    return (deg * Math.PI / 180.0);
  }

  /**
   * Calculates the great circle distance between two points on the Earth. Uses the Haversine
   * Formula.
   *
   * @param latitude1 Latitude of first location in decimal degrees.
   * @param longitude1 Longitude of first location in decimal degrees.
   * @param latitude2 Latitude of second location in decimal degrees.
   * @param longitude2 Longitude of second location in decimal degrees.
   * @return Distance in meter.
   */
  static double distance(
      final double latitude1,
      final double longitude1,
      final double latitude2,
      final double longitude2) {
    final double latitudeSin = Math.sin(Math.toRadians(latitude2 - latitude1) / 2);
    final double longitudeSin = Math.sin(Math.toRadians(longitude2 - longitude1) / 2);
    final double a =
        latitudeSin * latitudeSin
            + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * longitudeSin
                * longitudeSin;
    final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return 6378137 * c;
  }

  /**
   * Calculates distance between to points.
   *
   * <p>latitude: South is negative longitude: West is negative
   *
   * @param lat1 latitude of point 1 (in decimal degree)
   * @param lon1 longitude of point 1 ( " )
   * @param lat2 latitude of point 2 (in decimal degree)
   * @param lon2 longitude of point 2 ( " )
   * @param unit distance unit ('M'=statute miles, 'K'=kilometers (default), 'N'=nautical miles)
   * @return distance between point 1 and point 2 in given unit
   */
  static double distance(
      final double lat1,
      final double lon1,
      final double lat2,
      final double lon2,
      final String unit) {
    final double theta = lon1 - lon2;
    double dist =
        Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
            + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    if ("K".equals(unit)) {
      dist = dist * 1.609344;
    } else if ("N".equals(unit)) {
      dist = dist * 0.8684;
    }
    return (dist);
  }

  static String distanceInPreferredUnit(
      GeoLocation geoLocation1, GeoLocation geoLocation2, Unit<Length> preferredUnit) {
    final double distanceInMeter =
        distance(
            geoLocation1.getLatitude(),
            geoLocation1.getLongitude(),
            geoLocation2.getLatitude(),
            geoLocation2.getLongitude());
    final long distanceKm = Math.round(distanceInMeter / 1000);
    Amount<Length> distanceInKm = Amount.valueOf(distanceKm, SI.KILOMETER);
    long distanceInPreferredUnit = Math.round(distanceInKm.doubleValue(preferredUnit));
    String distance = String.valueOf(distanceInPreferredUnit) + " " + preferredUnit.toString();
    return distance;
  }

  /**
   * Returns the difference in degrees of latitude corresponding to the distance from the center
   * point. This distance can be used to find the extreme points.
   *
   * @param p1 geo point
   * @param distance distanc in km
   * @return extreme latitudes diff in km
   */
  static double getExtremeLatitudesDiffForPoint(final GeoLocation p1, final double distance) {
    final double latitudeRadians = distance / EARTH_RADIUS_KM;
    final double diffLat = rad2deg(latitudeRadians);
    return diffLat;
  }

  /**
   * Returns the difference in degrees of longitude corresponding to the distance from the center
   * point. This distance can be used to find the extreme points.
   *
   * @param p1 geo point
   * @param distance distance in km
   * @return extreme longitudes diff in km
   */
  static double getExtremeLongitudesDiffForPoint(final GeoLocation p1, final double distance) {
    double lat1 = p1.getLatitude();
    lat1 = deg2rad(lat1);
    final double longitudeRadius = Math.cos(lat1) * EARTH_RADIUS_KM;
    double diffLong = (distance / longitudeRadius);
    diffLong = rad2deg(diffLong);
    return diffLong;
  }

  /**
   * Returns an array of two extreme points corresponding to center point and the distance from the
   * center point. These extreme points are the points with min latitude and longitude and max
   * latitude and longitude.
   *
   * @param centerLocation location in latitude/longitude
   * @param distance distance radius [km]
   * @return minimum (first) and maximum (second) points
   */
  static GeoLocation[] getExtremePointsFrom(
      final GeoLocation centerLocation, final Double distance) {
    final double longDiff = getExtremeLongitudesDiffForPoint(centerLocation, distance);
    final double latDiff = getExtremeLatitudesDiffForPoint(centerLocation, distance);

    CoordinateLocation c1 =
        new CoordinateLocation(
            centerLocation.getLatitude() - latDiff,
            centerLocation.getLongitude() - longDiff,
            0d,
            0d);
    GeoLocation p1 = new GeoLocation();
    p1.setCoordinateLocation(c1);
    p1 = validatePoint(p1);

    CoordinateLocation c2 =
        new CoordinateLocation(
            centerLocation.getLatitude() + latDiff,
            centerLocation.getLongitude() + longDiff,
            0d,
            0d);
    GeoLocation p2 = new GeoLocation();
    p2.setCoordinateLocation(c2);
    p2 = validatePoint(p2);

    return new GeoLocation[] {p1, p2};
  }

  /**
   * Converts radians to decimal degrees
   *
   * @param rad radians
   * @return decimal degrees
   */
  static double rad2deg(final double rad) {
    return (rad * 180 / Math.PI);
  }

  /**
   * Validates if the point passed has valid values in degrees i.e. latitude lies between -90 and
   * +90 and the longitude
   *
   * @param point geo location
   * @return corrected geolocation
   */
  static GeoLocation validatePoint(final GeoLocation point) {
    if (point.getLatitude() > 90) {
      point.getCoordinateLocation().setLatitude(90 - (point.getLatitude() - 90));
    }
    if (point.getLatitude() < -90) {
      point.getCoordinateLocation().setLatitude(-90 - (point.getLatitude() + 90));
    }
    if (point.getLongitude() > 180) {
      point.getCoordinateLocation().setLongitude(-180 + (point.getLongitude() - 180));
    }
    if (point.getLongitude() < -180) {
      point.getCoordinateLocation().setLongitude(180 + (point.getLongitude() + 180));
    }
    return point;
  }
}
