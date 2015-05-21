package daris.client.model.query.filter.mf;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class GeoShapeFilter extends Filter {

    public static enum ContainerType {
        asset, content
    }

    public static enum GeoShapeOperator {
        inside, outside, intersects, covers
    }

    public static enum GeoShapeType {
        point, polyline, polygon, rectangle
    }

    private ContainerType _containerType;

    private GeoShapeOperator _op;

    private GeoShapeType _type;

    private List<GeoPoint> _points;

    public GeoShapeFilter(ContainerType containerType, GeoShapeOperator op, GeoShapeType type, GeoPoint p) {
        _containerType = containerType;
        _op = op;
        _type = type;
        _points = new Vector<GeoPoint>(1);
        _points.add(p);
    }

    public GeoShapeFilter(ContainerType containerType, GeoShapeOperator op, GeoShapeType type, List<GeoPoint> ps) {
        _containerType = containerType;
        _op = op;
        _type = type;
        _points = new Vector<GeoPoint>(ps);
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append(_containerType.toString() + " ");
        sb.append("geoshape ");
        sb.append(_op + " ");
        sb.append(_type + " ");
        sb.append("[");
        for (int i = 0; i < _points.size(); i++) {
            sb.append(_points.get(i).toString());
            if (i != _points.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        // TODO Auto-generated method stub

    }

    @Override
    public Validity valid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Filter copy() {
        // TODO Auto-generated method stub
        return null;
    }

}
