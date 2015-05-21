package daris.client.model.sc;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.util.DateTime;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.Timer;

import daris.client.model.sc.messages.ShoppingCartProcessingDescribe;
import daris.client.model.transcode.Transcode;
import daris.client.util.ByteUtil;

public class ShoppingCart {

    public static final String TYPE_NAME = "shoppingcart";

    private long _cartId;
    private Status _status;
    private Date _changed;
    private List<Log> _logs;
    private String _name;
    private String _description;
    private boolean _decompressArchive;
    private String _template;
    private Owner _owner;
    private boolean _canModify;
    private boolean _canReEdit;
    private boolean _canWithdraw;
    private boolean _canReprocess;
    private boolean _canDestroy;
    private String _assignedTo;
    private MetadataOutput _metadataOutput;
    private boolean _selfServiced;
    private int _numberofItems = 0;
    private long _sizeOfItems = 0;
    private Map<String, Integer> _mimeTypeCount;
    private Map<String, Transcode> _transcodes;
    private DeliveryDestination _destination;
    private Archive _archive;
    private Layout _layout;

    protected ShoppingCart(XmlElement ce) throws Throwable {

        /*
         * id
         */
        _cartId = ce.longValue("@id", 0);

        /*
         * status
         */
        _status = Status.fromString(ce.value("status"));

        /*
         * changed
         */
        _changed = ce.dateValue("status/@changed");

        /*
         * log
         */
        _logs = Log.instantiate(ce.elements("log"));

        /*
         * name
         */
        _name = ce.value("name");

        /*
         * description
         */
        _description = ce.value("description");

        /*
         * decompress-archive
         */
        _decompressArchive = ce.booleanValue("decompress-archive", true);

        /*
         * template
         */
        _template = ce.value("template");

        /*
         * owner
         */
        _owner = Owner.instantiate(ce.element("owner"));

        /*
         * access
         */
        _canModify = ce.booleanValue("access/can-modify", false);
        _canReEdit = ce.booleanValue("access/can-re-edit", false);
        _canWithdraw = ce.booleanValue("access/can-withdraw", false);
        _canReprocess = ce.booleanValue("access/can-reprocess", false);
        _canDestroy = ce.booleanValue("access/can-destroy", false);

        /*
         * assigned-to
         */
        _assignedTo = ce.value("assigned-to");

        /*
         * metadata-output
         */
        _metadataOutput = MetadataOutput.fromString(ce.value("metadata-output"));

        /*
         * self-serviced
         */
        _selfServiced = ce.booleanValue("self-serviced", true);

        /*
         * content-statistics
         */
        XmlElement cse = ce.element("content-statistics");
        if (cse != null) {
            _numberofItems = cse.intValue("item-count", 0);
            _sizeOfItems = cse.longValue("item-size", 0);
            XmlElement cmte = cse.element("content-mimetype");
            if (cmte != null) {
                List<XmlElement> nes = cse.elements("name");
                if (nes != null && !nes.isEmpty()) {
                    _mimeTypeCount = new HashMap<String, Integer>();
                    for (XmlElement ne : nes) {
                        _mimeTypeCount.put(ne.value(), ne.intValue("@count", 0));
                    }
                }
            }
        }

        /*
         * destination
         */
        _destination = new DeliveryDestination(ce);

        /*
         * archive
         */
        _archive = Archive.instantiate(ce);

        /*
         * layout & layout pattern
         */
        _layout = Layout.instantiate(ce.element("layout"));

        /*
         * data-transformation/transcode
         */
        List<XmlElement> tes = ce.elements("data-transformation/transcode");
        if (tes != null) {
            _transcodes = Transcode.instantiateMap(tes);
        }

    }

    public long id() {

        return _cartId;
    }

    public Status status() {

        return _status;
    }

    public Date changed() {
        return _changed;
    }

    public List<Log> logs() {
        if (_logs == null || _logs.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableList(_logs);
    }

    public Map<String, Integer> mimeTypeCounts() {
        if (_mimeTypeCount == null) {
            return null;
        }
        return Collections.unmodifiableMap(_mimeTypeCount);
    }

    public String name() {

        return _name;
    }

    public void setName(String name) {

        _name = name;
    }

    public String description() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public boolean decompressArchive() {
        return _decompressArchive;
    }

    public String template() {
        return _template;
    }

    public Owner owner() {
        return _owner;
    }

    public boolean canModify() {
        return _canModify;
    }

    public boolean canReEdit() {
        return _canReEdit;
    }

    public boolean canWithdraw() {
        return _canWithdraw;
    }

    public boolean canReprocess() {
        return _canReprocess;
    }

    public boolean canDestroy() {
        return _canDestroy;
    }

    public String assignedTo() {
        return _assignedTo;
    }

    public MetadataOutput medatadataOutput() {

        return _metadataOutput;
    }

    public void setMetadataOutput(MetadataOutput mo) {

        _metadataOutput = mo;
    }

    public boolean selfServiced() {
        return _selfServiced;
    }

    public int numberOfContentItems() {

        return _numberofItems;
    }

    public long sizeOfContentItems() {

        return _sizeOfItems;
    }

    public Collection<Transcode> transcodes() {
        if (_transcodes == null || _transcodes.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableCollection(_transcodes.values());
    }

    public void setTranscode(Transcode transcode) {

        if (_transcodes == null) {
            _transcodes = new LinkedHashMap<String, Transcode>();
        }
        _transcodes.put(transcode.from(), transcode);
    }

    public boolean hasTranscodes() {
        return _transcodes != null && !_transcodes.isEmpty();
    }

    public DeliveryDestination destination() {

        return _destination;
    }

    public void setDestination(DeliveryDestination destination) {

        if (destination == null) {
            return;
        }
        if (_destination != null && _destination.name().equals(destination.name())
                && _destination.method() == destination.method()) {
            return;
        }
        _destination = destination;
        if (_destination.method() == DeliveryMethod.deposit) {
            _archive = new Archive(Archive.Type.none);
        } else {
            if (_archive.type() == Archive.Type.none) {
                _archive = new Archive(Archive.Type.zip);
            }
        }
    }

    public Archive archive() {

        return _archive;
    }

    public void setArchive(Archive archive) {

        _archive = archive;
    }

    public Layout layout() {

        return _layout;
    }

    public void setLayout(Layout layout) {
        _layout = layout;
    }

    public void saveUpdateArgs(XmlWriter w) {

        w.add("sid", _cartId);

        if (_name != null) {
            w.add("name", _name);
        }

        if (_description != null) {
            w.add("description", _description);
        }

        /*
         * delivery
         */
        _destination.saveUpdateArgs(w);

        /*
         * layout
         */
        _layout.saveUpdateArgs(w);

        /*
         * arcive/packaging
         */
        _archive.saveUpdateArgs(w);

        /*
         * transcodes
         */
        if (hasTranscodes()) {
            w.push("data-transformation");
            Collection<Transcode> transcodes = _transcodes.values();
            for (Transcode transcode : transcodes) {
                w.push("transform");
                w.add("from", transcode.from());
                w.add("to", transcode.to());
                w.pop();
            }
            w.pop();
        }

        /*
         * metadata-output
         */
        if (_metadataOutput != null) {
            w.add("metadata-output", _metadataOutput);
        }

        /*
         * decompress-archive
         */
        w.add("decompress-archive", _decompressArchive);

    }

    public String summaryHTML() {
        return "<b>Shopping Cart " + _cartId + " [Status: " + _status.toString() + ", Number of Datasets: "
                + _numberofItems + ", Total Size: " + ByteUtil.humanReadableByteCount(_sizeOfItems, true) + "]</b>";
    }

    public String toHTML() {

        String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Shopping-cart</th></tr><thead>";
        html += "<tbody>";
        html += "<tr><td><b>id:</b></td><td>" + _cartId + "</td></tr>";
        if (_name != null) {
            html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
        }
        html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
        if (_numberofItems > 0) {
            html += "<tr><td><b>content:</b></td><td>" + _numberofItems + "items (size=" + _sizeOfItems + " bytes)";
            if (_mimeTypeCount != null) {
                for (String mimeType : _mimeTypeCount.keySet()) {
                    html += "<br/>" + mimeType + ": " + _mimeTypeCount.get(mimeType);
                }
            }
            html += "</td></tr>";
        }
        if (_destination != null) {
            html += "<tr><td><b>destination:</b></td><td>" + _destination.name() + "</td></tr>";
        }
        if (_archive != null) {
            if (!_archive.toString().equals(Archive.Type.none)) {
                html += "<tr><td><b>archive:</b></td><td>type: " + _archive.type().toString() + "</td></tr>";
            }
        }
        if (_transcodes != null) {
            for (Transcode transcode : _transcodes.values()) {
                html += "<tr><td><b>transcode:</b></td><td>from: " + transcode.from() + " to: " + transcode.to()
                        + "</td></tr>";
            }
        }
        if (_logs != null) {
            for (Log log : _logs) {
                html += "<tr><td><b>log:</b></td><td>[" + DateTime.dateTimeAsClientString(log.changed) + " status: "
                        + log.status + "] " + log.message + "</td></tr>";
            }
        }
        html += "</tbody></table>";
        return html;
    }

    public Timer monitorProgress(int delay, ProgressHandler ph) {
        return monitorProgress(id(), delay, ph);
    }

    public static Timer monitorProgress(final long scid, final int delay, final ProgressHandler ph) {
        Timer t = new Timer() {

            @Override
            public void run() {
                progress(this, delay, scid, ph);

            }
        };
        t.schedule(1);
        return t;
    }

    public static void progress(long scid, ProgressHandler ph) {
        progress(null, 0, scid, ph);
    }

    private static void progress(final Timer t, final int delay, long scid, final ProgressHandler ph) {
        if (scid <= 0) {
            ph.progress(null);
            return;
        }
        new ShoppingCartProcessingDescribe(scid).send(new ObjectMessageResponse<Progress>() {

            @Override
            public void responded(Progress progress) {
                if (t != null) {
                    if (progress == null) {
                        t.cancel();
                    } else {
                        t.schedule(delay);
                    }
                    ph.progress(progress);
                }
            }
        });
    }
    
    public boolean isActive(){
        return ActiveShoppingCart.isActive(_cartId);
    }

}
