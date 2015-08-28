package daris.client.model.repository;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class Repository extends DObject {

    public static class Server {
        private String _uuid;
        private String _version;
        private String _name;
        private String _organization;

        public Server(XmlElement se) {

            _uuid = se.value("uuid");
            _version = se.value("version");
            _name = se.value("name");
            _organization = se.value("organization");
        }

        public String uuid() {

            return _uuid;
        }

        public String name() {

            return _name;
        }

        public String version() {
            return _version;
        }

        public String organization() {

            return _organization;
        }
    }

    public static class Custodian {
        private String _address;
        private String _email;
        private String _prefix;
        private String _firstName;
        private String _middleName;
        private String _lastName;

        public Custodian(String prefix, String firstName, String middleName,
                String lastName, String address, String email) {

            _prefix = prefix;
            _firstName = firstName;
            _middleName = middleName;
            _lastName = lastName;
            _address = address;
            _email = email;
        }

        public Custodian(XmlElement ce) {

            _prefix = ce.value("prefix");
            _firstName = ce.value("first");
            _middleName = ce.value("middle");
            _lastName = ce.value("last");
            _address = ce.value("address");
            _email = ce.value("email");
        }

        public String address() {

            return _address;
        }

        public String email() {

            return _email;
        }

        public String prefix() {

            return _prefix;
        }

        public String firstName() {

            return _firstName;
        }

        public String middleName() {

            return _middleName;
        }

        public String lastName() {

            return _lastName;
        }

    }

    public static class Location {
        private String _building;
        private String _department;
        private String _institution;
        private String _precinct;

        public Location(String building, String department, String institution,
                String precinct) {

            _building = building;
            _department = department;
            _institution = institution;
            _precinct = precinct;
        }

        public Location(XmlElement le) {

            _building = le.value("building");
            _department = le.value("department");
            _institution = le.value("institution");
            _precinct = le.value("precinct");
        }

        public String building() {

            return _building;
        }

        public String department() {

            return _department;
        }

        public String institution() {

            return _institution;
        }

        public String precinct() {

            return _precinct;
        }

    }

    public static class DataHoldings {
        private String _description;
        private Date _startDate;

        public DataHoldings(Date startDate, String description) {

            _startDate = startDate;
            _description = description;
        }

        public DataHoldings(XmlElement dhe) {

            try {
                _startDate = dhe.dateValue("start-date");
            } catch (Throwable e) {
                e.printStackTrace(System.out);
            }
            _description = dhe.value("description");
        }

        public String description() {

            return _description;
        }

        public Date startDate() {

            return _startDate;
        }
    }

    private Server _server;
    private Location _location;
    private DataHoldings _dataHoldings;
    private Custodian _custodian;
    private String _rights;

    protected Repository(XmlElement re) throws Throwable {

        super(re);

        XmlElement e = re.element("server");
        if (e != null) {
            _server = new Server(e);
        }
        e = re.element("custodian");
        if (e != null) {
            _custodian = new Custodian(e);
        }
        e = re.element("location");
        if (e != null) {
            _location = new Location(e);
        }
        e = re.element("data-holdings");
        if (e != null) {
            _dataHoldings = new DataHoldings(e);
        }
        _rights = re.value("rights/description");
    }

    public Server server() {

        return _server;
    }

    public Location location() {

        return _location;
    }

    public void setLocation(String building, String department,
            String institution, String precinct) {

        Location location = new Location(building, department, institution,
                precinct);
        _location = location;
    }

    public DataHoldings dataHoldings() {

        return _dataHoldings;
    }

    public void setDataHoldings(Date startDate, String description) {

        DataHoldings dataHoldings = new DataHoldings(startDate, description);
        _dataHoldings = dataHoldings;
    }

    public Custodian custodian() {

        return _custodian;
    }

    public void setCustodian(String prefix, String firstName,
            String middleName, String lastName, String address, String email) {

        Custodian custodian = new Custodian(prefix, firstName, middleName,
                lastName, address, email);
        _custodian = custodian;
    }

    public String rights() {

        return _rights;
    }

    public void setRights(String rights) {

        _rights = rights;
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.repository;
    }

    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        throw new AssertionError("Creating repository object is not allowed.");
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        // TODO:
        return null;
    }

}
