package daris.client.ui.object;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import daris.client.model.object.DataContent;
import daris.client.util.ByteUtil;

public class DataContentFieldGroup {

    public static FieldGroup fieldGroupFor(DataContent o) {

        FieldGroup fg = new FieldGroup(new FieldDefinition("data", ConstantType.DEFAULT, null, null, 1, 1));
        Field<String> typeField = new Field<String>(new FieldDefinition("type", StringType.DEFAULT, null, null, 1, 1));
        typeField.setValue(o.mimeType());
        fg.add(typeField);
        Field<String> extField = new Field<String>(new FieldDefinition("extension", StringType.DEFAULT, null, null, 1,
                1));
        extField.setValue(o.extension());
        fg.add(extField);
        Field<String> sizeField = new Field<String>(new FieldDefinition("size", ConstantType.DEFAULT, "(units: "
                + o.sizeUnits() + ")", null, 1, 1));
        sizeField.setValue(ByteUtil.humanReadableByteCount(o.size(), true));
        fg.add(sizeField);
        if (o.checksum() != null) {
            Field<String> csumField = new Field<String>(new FieldDefinition("csum", StringType.DEFAULT, "(base: "
                    + o.checksumBase() + ")", null, 1, 1));
            csumField.setValue(o.checksum());
            fg.add(csumField);
        }
        Field<String> storeField = new Field<String>(new FieldDefinition("store", StringType.DEFAULT, null, null, 1, 1));
        storeField.setValue(o.store());
        fg.add(storeField);
        Field<String> urlField = new Field<String>(new FieldDefinition("url", StringType.DEFAULT, null, null, 1, 1));
        urlField.setValue(o.url());
        fg.add(urlField);
        return fg;
    }

}
