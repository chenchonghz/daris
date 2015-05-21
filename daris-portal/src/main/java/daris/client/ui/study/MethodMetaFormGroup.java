package daris.client.ui.study;

import arc.gui.form.FormEditMode;
import arc.mf.client.xml.XmlElement;
import daris.client.ui.form.XmlMetaFormGroup;

public class MethodMetaFormGroup extends XmlMetaFormGroup {

    public static final String DEFAULT_GROUP = "Default";

    public MethodMetaFormGroup(XmlElement xe, FormEditMode mode, boolean allowIncompleteMeta) {
        super(xe, mode, allowIncompleteMeta);
    }

    @Override
    protected String group(XmlElement e) {
        String ns = e.value("@ns");
        if (ns != null) {
            String[] tokens = ns.split("_");
            if (tokens.length == 3) {
                return tokens[2];
            }
        }
        return DEFAULT_GROUP;
    }

}
