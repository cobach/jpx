/*
 * Java GPX Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.jpx;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;
import static io.jenetics.jpx.Lists.immutable;
import static io.jenetics.jpx.Parsers.toDGPSStation;
import static io.jenetics.jpx.Parsers.toDegrees;
import static io.jenetics.jpx.Parsers.toDouble;
import static io.jenetics.jpx.Parsers.toDuration;
import static io.jenetics.jpx.Parsers.toFix;
import static io.jenetics.jpx.Parsers.toLatitude;
import static io.jenetics.jpx.Parsers.toLength;
import static io.jenetics.jpx.Parsers.toLongitude;
import static io.jenetics.jpx.Parsers.toSpeed;
import static io.jenetics.jpx.Parsers.toUInt;
import static io.jenetics.jpx.Parsers.toZonedDateTime;
import static io.jenetics.jpx.XMLReader.attr;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * A {@code WayPoint} represents a way-point, point of interest, or named
 * feature on a map.
 * <p>
 * Creating a {@code WayPoint}:
 * <pre>{@code
 * final WayPoint point = WayPoint.builder()
 *     .lat(48.2081743).lon(16.3738189).ele(160)
 *     .build();
 * }</pre>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since 1.0
 */
public final class WayPoint implements Point, Serializable {

	private static final long serialVersionUID = 1L;

	private final Latitude _latitude;
	private final Longitude _longitude;

	private final Length _elevation;
	private final Speed _speed;
	private final ZonedDateTime _time;
	private final Degrees _magneticVariation;
	private final Length _geoidHeight;
	private final String _name;
	private final String _comment;
	private final String _description;
	private final String _source;
	private final List<Link> _links;
	private final String _symbol;
	private final String _type;
	private final Fix _fix;
	private final UInt _sat;
	private final Double _hdop;
	private final Double _vdop;
	private final Double _pdop;
	private final Duration _ageOfGPSData;
	private final DGPSStation _dgpsID;

	/**
	 * Create a new way-point with the given parameter.
	 *
	 * @param latitude the latitude of the point, WGS84 datum (mandatory)
	 * @param longitude the longitude of the point, WGS84 datum (mandatory)
	 * @param elevation the elevation (in meters) of the point (optional)
	 * @param speed the current GPS speed (optional)
	 * @param time creation/modification timestamp for element. Conforms to ISO
	 *        8601 specification for date/time representation. Fractional seconds
	 *        are allowed for millisecond timing in tracklogs. (optional)
	 * @param magneticVariation the magnetic variation at the point (optional)
	 * @param geoidHeight height (in meters) of geoid (mean sea level) above
	 *        WGS84 earth ellipsoid. As defined in NMEA GGA message. (optional)
	 * @param name the GPS name of the way-point. This field will be transferred
	 *        to and from the GPS. GPX does not place restrictions on the length
	 *        of this field or the characters contained in it. It is up to the
	 *        receiving application to validate the field before sending it to
	 *        the GPS. (optional)
	 * @param comment GPS way-point comment. Sent to GPS as comment (optional)
	 * @param description a text description of the element. Holds additional
	 *        information about the element intended for the user, not the GPS.
	 *        (optional)
	 * @param source source of data. Included to give user some idea of
	 *        reliability and accuracy of data. "Garmin eTrex", "USGS quad
	 *        Boston North", e.g. (optional)
	 * @param links links to additional information about the way-point. May be
	 *        empty, but not {@code null}.
	 * @param symbol text of GPS symbol name. For interchange with other
	 *        programs, use the exact spelling of the symbol as displayed on the
	 *        GPS. If the GPS abbreviates words, spell them out. (optional)
	 * @param type type (classification) of the way-point (optional)
	 * @param fix type of GPX fix (optional)
	 * @param sat number of satellites used to calculate the GPX fix (optional)
	 * @param hdop horizontal dilution of precision (optional)
	 * @param vdop vertical dilution of precision (optional)
	 * @param pdop position dilution of precision. (optional)
	 * @param ageOfGPSData number of seconds since last DGPS update (optional)
	 * @param dgpsID ID of DGPS station used in differential correction (optional)
	 * @throws NullPointerException if the {@code latitude} or {@code longitude}
	 *         is {@code null}
	 */
	private WayPoint(
		final Latitude latitude,
		final Longitude longitude,
		final Length elevation,
		final Speed speed,
		final ZonedDateTime time,
		final Degrees magneticVariation,
		final Length geoidHeight,
		final String name,
		final String comment,
		final String description,
		final String source,
		final List<Link> links,
		final String symbol,
		final String type,
		final Fix fix,
		final UInt sat,
		final Double hdop,
		final Double vdop,
		final Double pdop,
		final Duration ageOfGPSData,
		final DGPSStation dgpsID
	) {
		_latitude = requireNonNull(latitude);
		_longitude = requireNonNull(longitude);

		_elevation = elevation;
		_speed = speed;
		_time = time;
		_magneticVariation = magneticVariation;
		_geoidHeight = geoidHeight;
		_name = name;
		_comment = comment;
		_description = description;
		_source = source;
		_links = immutable(links);
		_symbol = symbol;
		_type = type;
		_fix = fix;
		_sat = sat;
		_hdop = hdop;
		_vdop = vdop;
		_pdop = pdop;
		_ageOfGPSData = ageOfGPSData;
		_dgpsID = dgpsID;
	}

	@Override
	public Latitude getLatitude() {
		return _latitude;
	}

	@Override
	public Longitude getLongitude() {
		return _longitude;
	}

	@Override
	public Optional<Length> getElevation() {
		return Optional.ofNullable(_elevation);
	}

	/**
	 * The current GPS speed.
	 *
	 * @return the current GPS speed
	 */
	public Optional<Speed> getSpeed() {
		return Optional.ofNullable(_speed);
	}

	@Override
	public Optional<ZonedDateTime> getTime() {
		return Optional.ofNullable(_time);
	}

	/**
	 * The magnetic variation at the point.
	 *
	 * @return the magnetic variation at the point
	 */
	public Optional<Degrees> getMagneticVariation() {
		return Optional.ofNullable(_magneticVariation);
	}

	/**
	 * The height (in meters) of geoid (mean sea level) above WGS84 earth
	 * ellipsoid. As defined in NMEA GGA message.
	 *
	 * @return the height (in meters) of geoid (mean sea level) above WGS84
	 *         earth ellipsoid
	 */
	public Optional<Length> getGeoidHeight() {
		return Optional.ofNullable(_geoidHeight);
	}

	/**
	 * The GPS name of the way-point. This field will be transferred to and from
	 * the GPS. GPX does not place restrictions on the length of this field or
	 * the characters contained in it. It is up to the receiving application to
	 * validate the field before sending it to the GPS.
	 *
	 * @return the GPS name of the way-point
	 */
	public Optional<String> getName() {
		return Optional.ofNullable(_name);
	}

	/**
	 * The GPS way-point comment.
	 *
	 * @return the GPS way-point comment
	 */
	public Optional<String> getComment() {
		return Optional.ofNullable(_comment);
	}

	/**
	 * Return a text description of the element. Holds additional information
	 * about the element intended for the user, not the GPS.
	 *
	 * @return a text description of the element
	 */
	public Optional<String> getDescription() {
		return Optional.ofNullable(_description);
	}

	/**
	 * Return the source of data. Included to give user some idea of reliability
	 * and accuracy of data. "Garmin eTrex", "USGS quad Boston North", e.g.
	 *
	 * @return the source of the data
	 */
	public Optional<String> getSource() {
		return Optional.ofNullable(_source);
	}

	/**
	 * Return the links to additional information about the way-point.
	 *
	 * @return the links to additional information about the way-point
	 */
	public List<Link> getLinks() {
		return _links;
	}

	/**
	 * Return the text of GPS symbol name. For interchange with other programs,
	 * use the exact spelling of the symbol as displayed on the GPS. If the GPS
	 * abbreviates words, spell them out.
	 *
	 * @return the text of GPS symbol name
	 */
	public Optional<String> getSymbol() {
		return Optional.ofNullable(_symbol);
	}

	/**
	 * Return the type (classification) of the way-point.
	 *
	 * @return the type (classification) of the way-point
	 */
	public Optional<String> getType() {
		return Optional.ofNullable(_type);
	}

	/**
	 * Return the type of GPX fix.
	 *
	 * @return the type of GPX fix
	 */
	public Optional<Fix> getFix() {
		return Optional.ofNullable(_fix);
	}

	/**
	 * Return the number of satellites used to calculate the GPX fix.
	 *
	 * @return the number of satellites used to calculate the GPX fix
	 */
	public Optional<UInt> getSat() {
		return Optional.ofNullable(_sat);
	}

	/**
	 * Return the horizontal dilution of precision.
	 *
	 * @return the horizontal dilution of precision
	 */
	public Optional<Double> getHdop() {
		return Optional.ofNullable(_hdop);
	}

	/**
	 * Return the vertical dilution of precision.
	 *
	 * @return the vertical dilution of precision
	 */
	public Optional<Double> getVdop() {
		return Optional.ofNullable(_vdop);
	}

	/**
	 * Return the position dilution of precision.
	 *
	 * @return the position dilution of precision
	 */
	public Optional<Double> getPdop() {
		return Optional.ofNullable(_pdop);
	}

	/**
	 * Return the number of seconds since last DGPS update.
	 *
	 * @return number of seconds since last DGPS update
	 */
	public Optional<Duration> getAgeOfGPSData() {
		return Optional.ofNullable(_ageOfGPSData);
	}

	/**
	 * Return the ID of DGPS station used in differential correction.
	 *
	 * @return the ID of DGPS station used in differential correction
	 */
	public Optional<DGPSStation> getDGPSID() {
		return Optional.ofNullable(_dgpsID);
	}

	/**
	 * Convert the <em>immutable</em> way-point object into a <em>mutable</em>
	 * builder initialized with the current way-point values.
	 *
	 * @since !__version__!
	 *
	 * @return a new way-point builder initialized with the values of {@code this}
	 *         way-point
	 */
	public Builder toBuilder() {
		return builder()
			.lat(_latitude)
			.lon(_longitude)
			.ele(_elevation)
			.speed(_speed)
			.time(_time)
			.magvar(_magneticVariation)
			.geoidheight(_geoidHeight)
			.name(_name)
			.cmt(_comment)
			.desc(_description)
			.src(_source)
			.links(_links)
			.sym(_symbol)
			.type(_type)
			.fix(_fix)
			.sat(_sat)
			.hdop(_hdop)
			.vdop(_vdop)
			.pdop(_pdop)
			.ageofdgpsdata(_ageOfGPSData)
			.dgpsid(_dgpsID);
	}

	@Override
	public int hashCode() {
		int hash = 37;
		hash += 17*_latitude.hashCode() + 31;
		hash += 17*_longitude.hashCode() + 31;
		hash += 17*Objects.hashCode(_elevation) + 31;
		hash += 17*Objects.hashCode(_speed) + 31;
		hash += 17*Objects.hashCode(_time) + 31;
		hash += 17*Objects.hashCode(_magneticVariation) + 31;
		hash += 17*Objects.hashCode(_geoidHeight) + 31;
		hash += 17*Objects.hashCode(_name) + 31;
		hash += 17*Objects.hashCode(_comment) + 31;
		hash += 17*Objects.hashCode(_description) + 31;
		hash += 17*Objects.hashCode(_source) + 31;
		hash += 17*Objects.hashCode(_links) + 31;
		hash += 17*Objects.hashCode(_symbol) + 31;
		hash += 17*Objects.hashCode(_type) + 31;
		hash += 17*Objects.hashCode(_fix) + 31;
		hash += 17*Objects.hashCode(_sat) + 31;
		hash += 17*Objects.hashCode(_hdop) + 31;
		hash += 17*Objects.hashCode(_vdop) + 31;
		hash += 17*Objects.hashCode(_pdop) + 31;
		hash += 17*Objects.hashCode(_ageOfGPSData) + 31;
		hash += 17*Objects.hashCode(_dgpsID) + 31;

		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof WayPoint &&
			((WayPoint)obj)._latitude.equals(_latitude) &&
			((WayPoint)obj)._longitude.equals(_longitude) &&
			Objects.equals(((WayPoint)obj)._elevation, _elevation) &&
			Objects.equals(((WayPoint)obj)._speed, _speed) &&
			ZonedDateTimeFormat.equals(((WayPoint)obj)._time, _time) &&
			Objects.equals(((WayPoint)obj)._magneticVariation, _magneticVariation) &&
			Objects.equals(((WayPoint)obj)._geoidHeight, _geoidHeight) &&
			Objects.equals(((WayPoint)obj)._name, _name) &&
			Objects.equals(((WayPoint)obj)._comment, _comment) &&
			Objects.equals(((WayPoint)obj)._description, _description) &&
			Objects.equals(((WayPoint)obj)._source, _source) &&
			Objects.equals(((WayPoint)obj)._links, _links) &&
			Objects.equals(((WayPoint)obj)._symbol, _symbol) &&
			Objects.equals(((WayPoint)obj)._type, _type) &&
			Objects.equals(((WayPoint)obj)._fix, _fix) &&
			Objects.equals(((WayPoint)obj)._sat, _sat) &&
			Objects.equals(((WayPoint)obj)._hdop, _hdop) &&
			Objects.equals(((WayPoint)obj)._vdop, _vdop) &&
			Objects.equals(((WayPoint)obj)._pdop, _pdop) &&
			Objects.equals(((WayPoint)obj)._ageOfGPSData, _ageOfGPSData) &&
			Objects.equals(((WayPoint)obj)._dgpsID, _dgpsID);
	}

	@Override
	public String toString() {
		return format(
			"[lat=%s, lon=%s, ele=%s]",
			_latitude, _latitude, _elevation
		);
	}


	/**
	 * Builder for creating a way-point with different parameters.
	 * <p>
	 * Creating a {@code WayPoint}:
	 * <pre>{@code
	 * final WayPoint point = WayPoint.builder()
	 *     .lat(48.2081743).lon(16.3738189).ele(160)
	 *     .build();
	 * }</pre>
	 *
	 * @see  #builder()
	 */
	public static final class Builder {
		private Latitude _latitude;
		private Longitude _longitude;

		private Length _elevation;
		private Speed _speed;
		private ZonedDateTime _time;
		private Degrees _magneticVariation;
		private Length _geoidHeight;
		private String _name;
		private String _comment;
		private String _description;
		private String _source;
		private final List<Link> _links = new ArrayList<>();
		private String _symbol;
		private String _type;
		private Fix _fix;
		private UInt _sat;
		private Double _hdop;
		private Double _vdop;
		private Double _pdop;
		private Duration _ageOfDGPSData;
		private DGPSStation _dgpsID;

		Builder() {
		}

		public Builder map(final Consumer<Builder> builder) {
			return this;
		}

		/**
		 * Set the latitude value of the way-point.
		 *
		 * @param latitude the new latitude value
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws NullPointerException if the given value is {@code null}
		 */
		public Builder lat(final Latitude latitude) {
			_latitude = requireNonNull(latitude);
			return this;
		}

		/**
		 * Set the latitude value of the way-point.
		 *
		 * @param latitude the new latitude value
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the given value is not within the
		 *         range of {@code [-90..90]}
		 */
		public Builder lat(final double degrees) {
			return lat(Latitude.ofDegrees(degrees));
		}

		/**
		 * Return the current latitude value.
		 *
		 * @version !__version__!
		 *
		 * @return the current latitude value
		 */
		public Latitude lat() {
			return _latitude;
		}

		/**
		 * Set the longitude value of the way-point.
		 *
		 * @param longitude the new longitude value
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws NullPointerException if the given value is {@code null}
		 */
		public Builder lon(final Longitude longitude) {
			_longitude = requireNonNull(longitude);
			return this;
		}

		/**
		 * Set the longitude value of the way-point.
		 *
		 * @param longitude the new longitude value
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the given value is not within the
		 *         range of {@code [-180..180]}
		 */
		public Builder lon(final double degrees) {
			return lon(Longitude.ofDegrees(degrees));
		}

		/**
		 * Return the current longitude value.
		 *
		 * @version !__version__!
		 *
		 * @return the current longitude value
		 */
		public Longitude lon() {
			return _longitude;
		}

		/**
		 * Set the elevation  of the point.
		 *
		 * @param elevation the elevation of the point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder ele(final Length elevation) {
			_elevation = elevation;
			return this;
		}

		/**
		 * Set the elevation (in meters) of the point.
		 *
		 * @param meters the elevation of the point, in meters
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder ele(final double meters) {
			_elevation = Length.of(meters, Length.Unit.METER);
			return this;
		}

		/**
		 * Set the elevation of the point.
		 *
		 * @param meters the elevation of the point
		 * @param unit the length unit
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder ele(final double meters, final Length.Unit unit) {
			_elevation = Length.of(meters, unit);
			return this;
		}

		/**
		 * Return the current elevation value.
		 *
		 * @version !__version__!
		 *
		 * @return the current elevation value
		 */
		public Optional<Length> ele() {
			return Optional.ofNullable(_elevation);
		}

		/**
		 * Set the current GPS speed.
		 *
		 * @param speed the current GPS speed
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder speed(final Speed speed) {
			_speed = speed;
			return this;
		}

		/**
		 * Set the current GPS speed
		 *
		 * @param speed the current speed value
		 * @param unit the speed unit
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder speed(final double speed, final Speed.Unit unit) {
			return speed(Speed.of(speed, unit));
		}

		/**
		 * Set the current GPS speed.
		 *
		 * @param meterPerSecond the current GPS speed in m/s
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder speed(final double meterPerSecond) {
			_speed = Speed.of(meterPerSecond, Speed.Unit.METERS_PER_SECOND);
			return this;
		}

		/**
		 * Return the current speed value.
		 *
		 * @version !__version__!
		 *
		 * @return the current speed value
		 */
		public Optional<Speed> speed() {
			return Optional.ofNullable(_speed);
		}

		/**
		 * Set the creation/modification timestamp for the point.
		 *
		 * @param time the creation/modification timestamp for the point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder time(final ZonedDateTime time) {
			_time = time;
			return this;
		}

		/**
		 * Set the creation/modification timestamp for the point.
		 *
		 * @param instant the instant of the way-point
		 * @param zone the time-zone
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder time(final Instant instant, final ZoneId zone) {
			_time = instant != null
				? ZonedDateTime.ofInstant(instant, zone != null ? zone : UTC)
				: null;
			return this;
		}

		/**
		 * Set the creation/modification timestamp for the point.
		 *
		 * @param millis the instant of the way-point
		 * @param zone the time-zone
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder time(final long millis, final ZoneId zone) {
			_time = ZonedDateTime.ofInstant(
				Instant.ofEpochMilli(millis),
				zone != null ? zone : UTC
			);
			return this;
		}

		/**
		 * Set the creation/modification timestamp for the point. The zone is
		 * set to UTC.
		 *
		 * @param instant the instant of the way-point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder time(final Instant instant) {
			return time(instant, null);
		}

		/**
		 * Set the creation/modification timestamp for the point.
		 *
		 * @param millis the instant of the way-point
		 *        from
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder time(final long millis) {
			return time(Instant.ofEpochMilli(millis));
		}

		/**
		 * Return the current time value.
		 *
		 * @version !__version__!
		 *
		 * @return the current time value
		 */
		public Optional<ZonedDateTime> time() {
			return Optional.ofNullable(_time);
		}

		/**
		 * Set the magnetic variation at the point.
		 *
		 * @param variation the magnetic variation
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder magvar(final Degrees variation) {
			_magneticVariation = variation;
			return this;
		}

		/**
		 * Set the magnetic variation at the point.
		 *
		 * @param degree the magnetic variation
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the give value is not within the
		 *         range of {@code [0..360]}
		 */
		public Builder magvar(final double degree) {
			_magneticVariation = Degrees.ofDegrees(degree);
			return this;
		}

		/**
		 * Return the current magnetic variation value.
		 *
		 * @version !__version__!
		 *
		 * @return the current magnetic variation value
		 */
		public Optional<Degrees> magvar() {
			return Optional.ofNullable(_magneticVariation);
		}

		/**
		 * Set the height (in meters) of geoid (mean sea level) above WGS84 earth
		 * ellipsoid. As defined in NMEA GGA message.
		 *
		 * @param height the height (in meters) of geoid (mean sea level)
		 *        above WGS84 earth ellipsoid
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder geoidheight(final Length height) {
			_geoidHeight = height;
			return this;
		}

		/**
		 * Set the height (in meters) of geoid (mean sea level) above WGS84 earth
		 * ellipsoid. As defined in NMEA GGA message.
		 *
		 * @param meter the height (in meters) of geoid (mean sea level)
		 *        above WGS84 earth ellipsoid
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder geoidheight(final double meter) {
			_geoidHeight = Length.of(meter, Length.Unit.METER);
			return this;
		}

		/**
		 * Set the height of geoid (mean sea level) above WGS84 earth ellipsoid.
		 * As defined in NMEA GGA message.
		 *
		 * @param length the height of geoid (mean sea level) above WGS84 earth
		 *        ellipsoid
		 * @param unit the length unit
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder geoidheight(final double length, Length.Unit unit) {
			_geoidHeight = Length.of(length, unit);
			return this;
		}

		/**
		 * Return the current height of geoid value.
		 *
		 * @version !__version__!
		 *
		 * @return the current height of geoid value
		 */
		public Optional<Length> geoidheight() {
			return Optional.ofNullable(_geoidHeight);
		}

		/**
		 * Set the GPS name of the way-point. This field will be transferred to
		 * and from the GPS. GPX does not place restrictions on the length of
		 * this field or the characters contained in it. It is up to the
		 * receiving application to validate the field before sending it to the
		 * GPS.
		 *
		 * @param name the GPS name of the way-point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder name(final String name) {
			_name = name;
			return this;
		}

		/**
		 * Return the current name value.
		 *
		 * @version !__version__!
		 *
		 * @return the current name value
		 */
		public Optional<String> name() {
			return Optional.ofNullable(_name);
		}

		/**
		 * Set the GPS way-point comment.
		 *
		 * @param comment the GPS way-point comment.
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder cmt(final String comment) {
			_comment = comment;
			return this;
		}

		/**
		 * Return the current comment value.
		 *
		 * @version !__version__!
		 *
		 * @return the current comment value
		 */
		public Optional<String> cmt() {
			return Optional.ofNullable(_comment);
		}

		/**
		 * Set the GPS way-point description.
		 *
		 * @param description the GPS way-point description.
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder desc(final String description) {
			_description = description;
			return this;
		}

		/**
		 * Return the current description value.
		 *
		 * @version !__version__!
		 *
		 * @return the current description value
		 */
		public Optional<String> desc() {
			return Optional.ofNullable(_description);
		}

		/**
		 * Set the GPS way-point source.
		 *
		 * @param source the GPS way-point source.
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder src(final String source) {
			_source = source;
			return this;
		}

		/**
		 * Return the current source value.
		 *
		 * @version !__version__!
		 *
		 * @return the current source value
		 */
		public Optional<String> src() {
			return Optional.ofNullable(_source);
		}

		/**
		 * Set the links to additional information about the way-point.
		 *
		 * @param links the links to additional information about the way-point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder links(final List<Link> links) {
			_links.clear();
			if (links != null) {
				_links.addAll(links);
			}
			return this;
		}

		/**
		 * Set the links to external information about the way-point.
		 *
		 * @param link the links to external information about the way-point.
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder addLink(final Link link) {
			if (link != null) {
				_links.add(link);
			}
			return this;
		}

		/**
		 * Set the links to external information about the way-point.
		 *
		 * @param href the links to external information about the way-point.
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the given {@code href} is not a
		 *         valid URL
		 */
		public Builder addLink(final String href) {
			if (href != null) {
				_links.add(Link.of(href));
			}
			return this;
		}

		/**
		 * Return the current links.
		 *
		 * @version !__version__!
		 *
		 * @return the current links
		 */
		public List<Link> links() {
			return _links;
		}

		/**
		 * Set the text of GPS symbol name. For interchange with other programs,
		 * use the exact spelling of the symbol as displayed on the GPS. If the
		 * GPS abbreviates words, spell them out.
		 *
		 * @param symbol the text of GPS symbol name
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder sym(final String symbol) {
			_symbol = symbol;
			return this;
		}

		/**
		 * Return the current symbol value.
		 *
		 * @version !__version__!
		 *
		 * @return the current symbol value
		 */
		public Optional<String> sym() {
			return Optional.ofNullable(_symbol);
		}

		/**
		 * Set the type (classification) of the way-point.
		 *
		 * @param type the type (classification) of the way-point
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder type(final String type) {
			_type = type;
			return this;
		}

		/**
		 * Return the current type value.
		 *
		 * @version !__version__!
		 *
		 * @return the current type value
		 */
		public Optional<String> type() {
			return Optional.ofNullable(_type);
		}

		/**
		 * Set the type of GPX fix.
		 *
		 * @param fix the type of GPX fix
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder fix(final Fix fix) {
			_fix = fix;
			return this;
		}

		/**
		 * Set the type of GPX fix.
		 *
		 * @param fix the type of GPX fix
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the fix value is not one of the
		 *         following values: [none, 2d, 3d, dgps, pps]
		 */
		public Builder fix(final String fix) {
			try {
				_fix = toFix(fix, "WayPoint.fix");
			} catch (XMLStreamException e) {
				throw new IllegalArgumentException(e);
			}
			return this;
		}

		/**
		 * Return the current GPX fix value.
		 *
		 * @version !__version__!
		 *
		 * @return the current GPX fix value
		 */
		public Optional<Fix> fix() {
			return Optional.ofNullable(_fix);
		}

		/**
		 * Set the number of satellites used to calculate the GPX fix.
		 *
		 * @param sat the number of satellites used to calculate the GPX fix
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder sat(final UInt sat) {
			_sat = sat;
			return this;
		}

		/**
		 * Set the number of satellites used to calculate the GPX fix.
		 *
		 * @param sat the number of satellites used to calculate the GPX fix
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the given {@code value} is smaller
		 *         than zero
		 */
		public Builder sat(final int sat) {
			_sat = UInt.of(sat);
			return this;
		}

		/**
		 * Return the current number of satelites.
		 *
		 * @version !__version__!
		 *
		 * @return the current number of satelites
		 */
		public Optional<UInt> sat() {
			return Optional.ofNullable(_sat);
		}

		/**
		 * Set the horizontal dilution of precision.
		 *
		 * @param hdop the horizontal dilution of precision
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder hdop(final Double hdop) {
			_hdop = hdop;
			return this;
		}

		/**
		 * Return the current horizontal dilution.
		 *
		 * @version !__version__!
		 *
		 * @return the current horizontal dilution
		 */
		public Optional<Double> hdop() {
			return Optional.ofNullable(_hdop);
		}

		/**
		 * Set the vertical dilution of precision.
		 *
		 * @param vdop the vertical dilution of precision
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder vdop(final Double vdop) {
			_vdop = vdop;
			return this;
		}

		/**
		 * Return the current vertical dilution.
		 *
		 * @version !__version__!
		 *
		 * @return the current vertical dilution
		 */
		public Optional<Double> vdop() {
			return Optional.ofNullable(_vdop);
		}

		/**
		 * Set the position dilution of precision.
		 *
		 * @param pdop the position dilution of precision
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder pdop(final Double pdop) {
			_pdop = pdop;
			return this;
		}

		/**
		 * Return the current position dilution.
		 *
		 * @version !__version__!
		 *
		 * @return the current position dilution
		 */
		public Optional<Double> pdop() {
			return Optional.ofNullable(_pdop);
		}

		/**
		 * Set the age since last DGPS update.
		 *
		 * @param age the age since last DGPS update
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder ageofdgpsdata(final Duration age) {
			_ageOfDGPSData = age;
			return this;
		}

		/**
		 * Set the number of seconds since last DGPS update.
		 *
		 * @param seconds the age since last DGPS update
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder ageofdgpsdata(final double seconds) {
			_ageOfDGPSData = Duration.ofMillis((long)(seconds*1000));
			return this;
		}

		/**
		 * Return the current age since last DGPS update.
		 *
		 * @version !__version__!
		 *
		 * @return the current age since last DGPS update
		 */
		public Optional<Duration> ageofdgpsdata() {
			return Optional.ofNullable(_ageOfDGPSData);
		}

		/**
		 * Set the ID of DGPS station used in differential correction.
		 *
		 * @param station the ID of DGPS station used in differential correction
		 * @return {@code this} {@code Builder} for method chaining
		 */
		public Builder dgpsid(final DGPSStation station) {
			_dgpsID = station;
			return this;
		}

		/**
		 * Set the ID of DGPS station used in differential correction.
		 *
		 * @param station the ID of DGPS station used in differential correction
		 * @return {@code this} {@code Builder} for method chaining
		 * @throws IllegalArgumentException if the given station number is not in the
		 *         range of {@code [0..1023]}
		 */
		public Builder dgpsid(final int station) {
			_dgpsID = DGPSStation.of(station);
			return this;
		}

		/**
		 * Return the current the ID of DGPS station used in differential
		 * correction.
		 *
		 * @version !__version__!
		 *
		 * @return the current the ID of DGPS station used in differential
		 *         correction
		 */
		public Optional<DGPSStation> dgpsid() {
			return Optional.ofNullable(_dgpsID);
		}

		/**
		 * Create a new way-point with the given latitude and longitude value.
		 *
		 * @param latitude the latitude of the way-point
		 * @param longitude the longitude of the way-point
		 * @return a newly created way-point
		 */
		public WayPoint build(final Latitude latitude, final Longitude longitude) {
			lat(latitude);
			lon(longitude);
			return build();
		}

		/**
		 * Create a new way-point with the given latitude and longitude value.
		 *
		 * @param latitude the latitude of the way-point
		 * @param longitude the longitude of the way-point
		 * @return a newly created way-point
		 */
		public WayPoint build(final double latitude, final double longitude) {
			return build(Latitude.ofDegrees(latitude), Longitude.ofDegrees(longitude));
		}

		/**
		 * Build a new way-point from the current builder state.
		 *
		 * @return a new way-point from the current builder state
		 */
		public WayPoint build() {
			return new WayPoint(
				_latitude,
				_longitude,
				_elevation,
				_speed,
				_time,
				_magneticVariation,
				_geoidHeight,
				_name,
				_comment,
				_description,
				_source,
				_links,
				_symbol,
				_type,
				_fix,
				_sat,
				_hdop,
				_vdop,
				_pdop,
				_ageOfDGPSData,
				_dgpsID
			);
		}

	}

	/**
	 * Return a new {@code WayPoint} builder.
	 *
	 * @return a new {@code WayPoint} builder
	 */
	public static Builder builder() {
		return new Builder();
	}


	/* *************************************************************************
	 *  Static object creation methods
	 * ************************************************************************/

	/**
	 * Create a new {@code WayPoint} with the given {@code latitude} and
	 * {@code longitude} value.
	 *
	 * @param latitude the latitude of the point
	 * @param longitude the longitude of the point
	 * @return a new {@code WayPoint}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static WayPoint of(
		final Latitude latitude,
		final Longitude longitude
	) {
		return new WayPoint(
			latitude,
			longitude,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}

	/**
	 * Create a new {@code WayPoint} with the given {@code latitude} and
	 * {@code longitude} value.
	 *
	 * @param latitudeDegree the latitude of the point
	 * @param longitudeDegree the longitude of the point
	 * @return a new {@code WayPoint}
	 * @throws IllegalArgumentException if the given latitude or longitude is not
	 *         in the valid range.
	 */
	public static WayPoint of(
		final double latitudeDegree,
		final double longitudeDegree
	) {
		return of(
			Latitude.ofDegrees(latitudeDegree),
			Longitude.ofDegrees(longitudeDegree)
		);
	}

	/**
	 * Create a new {@code WayPoint} with the given parameters.
	 *
	 * @param latitude the latitude of the point
	 * @param longitude the longitude of the point
	 * @param time the timestamp of the way-point
	 * @return a new {@code WayPoint}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static WayPoint of(
		final Latitude latitude,
		final Longitude longitude,
		final ZonedDateTime time
	) {
		return new WayPoint(
			latitude,
			longitude,
			null,
			null,
			time,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}

	/**
	 * Create a new {@code WayPoint} with the given parameters.
	 *
	 * @param latitudeDegree the latitude of the point
	 * @param longitudeDegree the longitude of the point
	 * @param timeEpochMilli the timestamp of the way-point
	 * @return a new {@code WayPoint}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static WayPoint of(
		final double latitudeDegree,
		final double longitudeDegree,
		final long timeEpochMilli
	) {
		return of(
			Latitude.ofDegrees(latitudeDegree),
			Longitude.ofDegrees(longitudeDegree),
			ZonedDateTime.ofInstant(
				Instant.ofEpochMilli(timeEpochMilli),
				UTC
			)
		);
	}

	/**
	 * Create a new {@code WayPoint} with the given parameters.
	 *
	 * @param latitude the latitude of the point
	 * @param longitude the longitude of the point
	 * @param elevation the elevation of the point
	 * @param time the timestamp of the way-point
	 * @return a new {@code WayPoint}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static WayPoint of(
		final Latitude latitude,
		final Longitude longitude,
		final Length elevation,
		final ZonedDateTime time
	) {
		return new WayPoint(
			latitude,
			longitude,
			elevation,
			null,
			time,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}

	/**
	 * Create a new {@code WayPoint} with the given parameters.
	 *
	 * @param latitudeDegree the latitude of the point
	 * @param longitudeDegree the longitude of the point
	 * @param elevationMeter the elevation of the point
	 * @param timeEpochMilli the timestamp of the way-point
	 * @return a new {@code WayPoint}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static WayPoint of(
		final double latitudeDegree,
		final double longitudeDegree,
		final double elevationMeter,
		final long timeEpochMilli
	) {
		return of(
			Latitude.ofDegrees(latitudeDegree),
			Longitude.ofDegrees(longitudeDegree),
			Length.of(elevationMeter, Length.Unit.METER),
			ZonedDateTime.ofInstant(
				Instant.ofEpochMilli(timeEpochMilli),
				UTC
			)
		);
	}

	/**
	 * Create a new way-point with the given parameter.
	 *
	 * @param latitude the latitude of the point, WGS84 datum (mandatory)
	 * @param longitude the longitude of the point, WGS84 datum (mandatory)
	 * @param elevation the elevation (in meters) of the point (optional)
	 * @param speed the current GPS speed (optional)
	 * @param time creation/modification timestamp for element. Conforms to ISO
	 *        8601 specification for date/time representation. Fractional seconds
	 *        are allowed for millisecond timing in tracklogs. (optional)
	 * @param magneticVariation the magnetic variation at the point (optional)
	 * @param geoidHeight height (in meters) of geoid (mean sea level) above
	 *        WGS84 earth ellipsoid. As defined in NMEA GGA message. (optional)
	 * @param name the GPS name of the way-point. This field will be transferred
	 *        to and from the GPS. GPX does not place restrictions on the length
	 *        of this field or the characters contained in it. It is up to the
	 *        receiving application to validate the field before sending it to
	 *        the GPS. (optional)
	 * @param comment GPS way-point comment. Sent to GPS as comment (optional)
	 * @param description a text description of the element. Holds additional
	 *        information about the element intended for the user, not the GPS.
	 *        (optional)
	 * @param source source of data. Included to give user some idea of
	 *        reliability and accuracy of data. "Garmin eTrex", "USGS quad
	 *        Boston North", e.g. (optional)
	 * @param links links to additional information about the way-point. May be
	 *        empty, but not {@code null}.
	 * @param symbol text of GPS symbol name. For interchange with other
	 *        programs, use the exact spelling of the symbol as displayed on the
	 *        GPS. If the GPS abbreviates words, spell them out. (optional)
	 * @param type type (classification) of the way-point (optional)
	 * @param fix type of GPX fix (optional)
	 * @param sat number of satellites used to calculate the GPX fix (optional)
	 * @param hdop horizontal dilution of precision (optional)
	 * @param vdop vertical dilution of precision (optional)
	 * @param pdop position dilution of precision. (optional)
	 * @param ageOfGPSData number of seconds since last DGPS update (optional)
	 * @param dgpsID ID of DGPS station used in differential correction (optional)
	 * @throws NullPointerException if the {@code latitude} or {@code longitude}
	 *         is {@code null}
	 * @return a new {@code WayPoint}
	 */
	public static WayPoint of(
		final Latitude latitude,
		final Longitude longitude,
		final Length elevation,
		final Speed speed,
		final ZonedDateTime time,
		final Degrees magneticVariation,
		final Length geoidHeight,
		final String name,
		final String comment,
		final String description,
		final String source,
		final List<Link> links,
		final String symbol,
		final String type,
		final Fix fix,
		final UInt sat,
		final Double hdop,
		final Double vdop,
		final Double pdop,
		final Duration ageOfGPSData,
		final DGPSStation dgpsID
	) {
		return new WayPoint(
			latitude,
			longitude,
			elevation,
			speed,
			time,
			magneticVariation,
			geoidHeight,
			name,
			comment,
			description,
			source,
			links,
			symbol,
			type,
			fix,
			sat,
			hdop,
			vdop,
			pdop,
			ageOfGPSData,
			dgpsID
		);
	}


	/* *************************************************************************
	 *  XML stream object serialization
	 * ************************************************************************/

	/**
	 * Writes this {@code Link} object to the given XML stream {@code writer}.
	 *
	 * @param writer the XML data sink
	 * @throws XMLStreamException if an error occurs
	 */
	void write(final String name, final XMLStreamWriter writer)
		throws XMLStreamException
	{
		final XMLWriter xml = new XMLWriter(writer);

		xml.write(name,
			xml.attr("lat", _latitude),
			xml.attr("lon", _longitude),
			xml.elem("ele", _elevation, Length::doubleValue),
			xml.elem("speed", _speed ,Speed::doubleValue),
			xml.elem("time", ZonedDateTimeFormat.format(_time)),
			xml.elem("magvar", _magneticVariation, Degrees::doubleValue),
			xml.elem("geoidheight", _geoidHeight, Length::doubleValue),
			xml.elem("name", _name),
			xml.elem("cmt", _comment),
			xml.elem("desc", _description),
			xml.elem("src", _source),
			xml.elems(_links, Link::write),
			xml.elem("sym", _symbol),
			xml.elem("type", _type),
			xml.elem("fix", _fix, Fix::getValue),
			xml.elem("sat", _sat),
			xml.elem("hdop", _hdop),
			xml.elem("vdop", _vdop),
			xml.elem("pdop", _pdop),
			xml.elem("ageofdgpsdata", _ageOfGPSData, Duration::getSeconds),
			xml.elem("dgpsid", _dgpsID)
		);
	}

	@SuppressWarnings("unchecked")
	static XMLReader<WayPoint> reader(final String name) {
		final XML.Function<Object[], WayPoint> create = a -> WayPoint.builder()
			.ele(toLength(a[2], "WayPoint.ele"))
			.speed(toSpeed(a[3], "WayPoint.speed"))
			.time(toZonedDateTime((String)a[4]))
			.magvar(toDegrees(a[5], "WayPoint.magvar"))
			.geoidheight(toLength(a[6], "WayPoint.geoidheight"))
			.name(Parsers.toString(a[7]))
			.cmt(Parsers.toString(a[8]))
			.desc(Parsers.toString(a[9]))
			.src(Parsers.toString(a[10]))
			.links((List<Link>)a[11])
			.sym(Parsers.toString(a[12]))
			.type(Parsers.toString(a[13]))
			.fix(toFix(a[14], "WayPoint.fix"))
			.sat(toUInt(a[15], "WayPoint.sat"))
			.hdop(toDouble(a[16], "WayPoint.hdop"))
			.vdop(toDouble(a[17], "WayPoint.vdop"))
			.pdop(toDouble(a[18], "WayPoint.pdop"))
			.ageofdgpsdata(toDuration(a[19], "WayPoint.ageofdgpsdata"))
			.dgpsid(toDGPSStation(a[20], "WayPoint.dgpsid"))
			.build(
				toLatitude(a[0], "WayPoint.lat"),
				toLongitude(a[1], "WayPoint.lon"));

		return XMLReader.of(create, name,
			attr("lat"),
			attr("lon"),
			XMLReader.of("ele"),
			XMLReader.of("speed"),
			XMLReader.of("time"),
			XMLReader.of("magvar"),
			XMLReader.of("geoidheight"),
			XMLReader.of("name"),
			XMLReader.of("cmt"),
			XMLReader.of("desc"),
			XMLReader.of("src"),
			XMLReader.ofList(Link.reader()),
			XMLReader.of("sym"),
			XMLReader.of("type"),
			XMLReader.of("fix"),
			XMLReader.of("sat"),
			XMLReader.of("hdop"),
			XMLReader.of("vdop"),
			XMLReader.of("pdop"),
			XMLReader.of("ageofdgpsdata"),
			XMLReader.of("dgpsid")
		);
	}

}
