package daris.client.ui.form;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldSet;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.XmlType;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Predicate;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlAttribute;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.InvisibleConstantType;
import arc.mf.dtype.StringType;
import arc.mf.xml.defn.Attribute;
import arc.mf.xml.defn.Element;
import arc.mf.xml.defn.Node;
import arc.mf.xml.defn.Value;

@SuppressWarnings("unchecked")
public class XmlMetaForm {

    private static boolean isHiddenDocAttribute(XmlAttribute attr) {
        return attr != null && attr.name() != null
                && ("id".equals(attr.name()) || "ns".equals(attr.name())
                        || "tag".equals(attr.name())
                        || attr.name().startsWith("xmlns:"));
    }

    /**
     * Generate a form from a list of docs.
     * 
     * @param docs
     *            the doc elements.
     * @param mode
     *            the form edit mode.
     * @return
     */
    public static Form formFor(List<XmlElement> docs, FormEditMode mode) {
        Form form = new Form(mode);
        form.setBooleanAs(BooleanAs.TRUE_FALSE);
        if (FormEditMode.READ_ONLY == mode) {
            addDocsForView(form, docs);
        } else {
            addDocsForEdit(form, docs);
        }
        form.render();
        return form;
    }

    /**
     * Generate a form from a xml element that contains the docs.
     * 
     * @param root
     *            the root (container) element that contains the doc elements.
     * @param mode
     *            the form edit mode
     * @return
     */
    public static Form formFor(XmlElement root, FormEditMode mode) {

        assert root.name().equals("meta") || root.name().equals("public")
                || root.name().equals("private") || root.name().equals("method")
                || root.name().equals("subject");
        Form form = new Form(mode);
        form.setBooleanAs(BooleanAs.TRUE_FALSE);
        if (FormEditMode.READ_ONLY == mode) {
            addDocsForView(form, root.elements());
        } else {
            addDocsForEdit(form, root.elements("metadata"));
        }
        return form;
    }

    public static void addDocsForEdit(Form form, List<XmlElement> docs) {
        if (docs != null) {
            for (XmlElement doc : docs) {
                addDocForEdit(form, doc);
            }
        }
    }

    public static void addDocsForView(Form form, List<XmlElement> docs) {
        if (docs != null) {
            for (XmlElement doc : docs) {
                addDocForView(form, doc);
            }
        }
    }

    public static void addDoc(Form form, XmlElement doc, FormEditMode mode) {
        if (mode == FormEditMode.READ_ONLY) {
            addDocForView(form, doc);
        } else {
            addDocForEdit(form, doc);
        }
    }

    public static void addDocForEdit(Form form, XmlElement doc) {

        assert form.editMode() != FormEditMode.READ_ONLY;
        assert doc.name().equals("metadata");
        arc.mf.xml.defn.Element de = new arc.mf.xml.defn.Element(
                doc.value("@type"));
        de.setMinOccurs(doc.value("@requirement").equals("optional") ? 0 : 1);
        de.setMaxOccurs(1);
        de.setDescription(doc.value("description"));
        de.setDataType(DocType.DEFAULT);
        FieldGroup fg = new FieldGroup(new FieldDefinition(de.name(), de.type(),
                de.description(), null, de.minOccurs(), de.maxOccurs())) {
            public Validity valid() {
                Validity v = super.valid();
                if (v.valid() && !hasSomeValue() && definition().minOccurs() > 0
                        && !form().allowMissingMandatory()) {
                    return new IsNotValid("The mandatory document "
                            + definition().name() + " is missing some value.");
                }
                return v;
            }
        };

        List<XmlAttribute> attrs = doc.attributes();
        if (attrs != null && !attrs.isEmpty()) {
            for (XmlAttribute attr : attrs) {

                if (!(attr.name().equals("tag") || attr.name().equals("ns"))) {
                    // Only ns, tag attributes are allowed, others are filtered
                    // out since they cause trouble.
                    continue;
                }
                Field<String> af = new Field<String>(new FieldDefinition(
                        attr.name(), ConstantType.DEFAULT, null, null, 0, 1)) {
                    @Override
                    public boolean hasSomeValue() {
                        // TODO: use Jason's library if he can do this in his
                        // library.
                        // NOTE: ignores the top level attributes of the doc
                        // element.
                        return false;
                    }

                    @Override
                    public void save(XmlWriter w,
                            Predicate<FormItem<String>> p) {
                        // TODO: use Jason's library if he can do this in his
                        // library.
                        // NOTE: since hasSomeValue() always returns false. This
                        // method has to be overridden to avoid
                        // calling hasSomeValue() method.
                        if (value() != null && (p == null || p.eval(this))) {
                            if (definition().name() == null) {
                                w.appValue(valueAsServerString());
                            } else {
                                w.add(definition().name(),
                                        valueAsServerString());
                            }
                        }
                    }
                };
                af.setXmlType(XmlType.ATTRIBUTE);
                af.setVisible(false);
                af.setReadOnly();
                af.setInitialValue(attr.value());
                fg.add(af);
            }
        }
        List<XmlElement> xdes = doc.elements("definition/element");
        if (xdes != null) {
            for (XmlElement xde : xdes) {
                try {
                    arc.mf.xml.defn.Element sde = new arc.mf.xml.defn.Element(
                            de, xde);
                    de.add(sde, false);
                } catch (Throwable t) {
                    UnhandledException.report(null, t);
                }
            }
        }
        addMembersToFieldSetForEdit(fg, de);
        form.add(fg);
    }

    public static void addDocForView(Form form, XmlElement doc) {

        assert form.editMode() == FormEditMode.READ_ONLY;
        if (!doc.hasElements() && !doc.hasAttributes()) {
            Field<String> f = new Field<String>(new FieldDefinition(doc.name(),
                    StringType.DEFAULT, null, null, 0, 1));
            if (doc.value() != null) {
                f.setValue(doc.value(), false);
            }
            form.add(f);
        } else {
            FieldGroup fg = new FieldGroup(new FieldDefinition(doc.name(),
                    StringType.DEFAULT, null, null, 1, 1));
            List<XmlElement> ces = doc.elements();
            if (ces != null) {
                for (XmlElement ce : ces) {
                    addToFieldSetForView(fg, ce);
                }
            }
            List<XmlAttribute> as = doc.attributes();
            if (as != null) {
                for (XmlAttribute a : as) {
                    if (isHiddenDocAttribute(a)) {
                        continue;
                    }
                    Field<String> af = new Field<String>(new FieldDefinition(
                            a.name(), StringType.DEFAULT, null, null, 0, 1));
                    af.setValue(a.value(), false);
                    af.setXmlType(XmlType.ATTRIBUTE);
                    fg.add(af);
                }
            }
            if (doc.value() != null) {
                Field<String> vf = new Field<String>(new FieldDefinition(null,
                        StringType.DEFAULT, null, null, 1, 1));
                vf.setValue(doc.value(), false);
                fg.add(vf);
            }
            form.add(fg);
        }
    }

    private static void addMembersToFieldSetForEdit(FieldSet fs,
            arc.mf.xml.defn.Element de) {
        addMembersToFieldSetForEdit(fs, de, true);
    }

    @SuppressWarnings("rawtypes")
    private static void addMembersToFieldSetForEdit(FieldSet fs,
            arc.mf.xml.defn.Element de, boolean honourConstantFields) {
        List<Attribute> attrs = de.attributes();
        if (attrs != null) {
            for (Attribute a : attrs) {
                FieldDefinition fd = new FieldDefinition(a.name(), a.type(),
                        a.description(), null, a.minOccurs(), 1);
                Field af = new Field(fd);
                af.setXmlType(FormItem.XmlType.ATTRIBUTE);
                af.setVisible(a.visible());
                setValue(af, a, honourConstantFields);
                fs.add(af);
            }
        }
        List<Element> eles = de.elements();
        if (eles != null) {
            FieldDefinition pfd = null;
            for (Element se : eles) {

                FieldDefinition fd = null;

                // Repeat instance of the same definition?
                if (pfd != null) {
                    if (se.name().equals(pfd.name())) {
                        fd = pfd;
                    }
                }

                if (fd == null) {
                    fd = new FieldDefinition(se.name(), se.type(),
                            se.description(), null, se.minOccurs(),
                            se.maxOccurs());
                }

                if (se.hasSubNodes()) {
                    FieldGroup fg = new FieldGroup(fd);
                    addMembersToFieldSetForEdit(fg, se, honourConstantFields);
                    fs.add(fg);
                } else {
                    Field ef = new Field(fd);
                    setValue(ef, se, honourConstantFields);
                    fs.add(ef);
                }

                pfd = fd;
            }
        }

        if (de.hasValue()) {
            // The value part at this point can only have a max value of 1 - the
            // multi-value
            // occurs at the outer definition.
            FieldDefinition fd = new FieldDefinition(null, de.type(),
                    de.description(), null, 1, 1);
            Field ef = new Field(fd);
            setValue(ef, de, honourConstantFields);
            fs.add(ef);
        }

        // TODO - references..
    }

    /**
     * Sets the fields value and configues the fields read only state. Also
     * allows for override of read only state
     * 
     * @param f
     * @param n
     * @param honourConstantFields
     */
    @SuppressWarnings("rawtypes")
    private static void setValue(Field f, Node n,
            boolean honourConstantFields) {
        Value v = n.value();
        if (v == null) {
            return;
        }

        if (v.binding().constant() && honourConstantFields) {
            f.setReadOnly();
        }

        f.setInitialValue(v.value());
    }

    private static void addToFieldSetForView(FieldSet fs, XmlElement e) {

        FieldDefinition fd = new FieldDefinition(e.name(), ConstantType.DEFAULT,
                null, null, 1, 1);
        if (!e.hasAttributes() && !e.hasElements()) {
            // no sub-nodes
            Field<String> f = new Field<String>(fd);
            f.setXmlType(XmlType.ELEMENT);
            if (e.value() != null) {
                f.setValue(e.value());
            }
            f.setXmlType(XmlType.ELEMENT);
            fs.add(f);
            return;
        }
        FieldGroup fg = new FieldGroup(fd);
        if (e.hasAttributes()) {
            // has attributes
            for (XmlAttribute a : e.attributes()) {
                Field<String> af = new Field<String>(new FieldDefinition(
                        a.name(), ConstantType.DEFAULT, null, null, 1, 1));
                af.setXmlType(XmlType.ATTRIBUTE);
                if (a.value() != null) {
                    af.setValue(a.value());
                }
                fg.add(af);
            }
        }
        if (e.value() != null) {
            // has value and (has attributes/sub-elements)
            Field<String> f = new Field<String>(new FieldDefinition(null,
                    ConstantType.DEFAULT, null, null, 1, 1));
            f.setValue(e.value());
            fg.add(f);
        }
        List<XmlElement> ces = e.elements();
        if (e.hasElements()) {
            // has sub-elements
            for (XmlElement ce : ces) {
                addToFieldSetForView(fg, ce);
            }
        }
        fs.add(fg);
    }
}
