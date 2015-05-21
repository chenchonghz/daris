package daris.client.model.query.filter.mf;

public class GeoPoint {

	private double _latitude;
	private double _longitude;
	private String _evaluation;

	public GeoPoint(double latitude, double longitude, String evaluation) {
		_latitude = latitude;
		_longitude = longitude;
		_evaluation = evaluation;
	}

	public GeoPoint(double latitude, double longitude) {
		this(latitude, longitude, null);
	}

	public double latitude() {
		return _latitude;
	}

	public double longitude() {
		return _longitude;
	}

	public String evaluation() {
		return _evaluation;
	}

	public String toString() {
		return "(" + _latitude + "," + _longitude + (_evaluation == null ? ")" : ("," + _evaluation + ")"));
	}
}
