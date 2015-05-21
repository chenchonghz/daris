package daris.client.ui.announcement;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import daris.client.model.announcement.Announcement;

public class AnnouncementForm extends Form {

    public AnnouncementForm(Announcement a) {
        super(FormEditMode.READ_ONLY);
        Field<Long> uid = new Field<Long>(new FieldDefinition("uid",
                ConstantType.DEFAULT, "The unique id of the announcement.",
                null, 1, 1));
        uid.setValue(a.uid());
        add(uid);
        Field<String> title = new Field<String>(new FieldDefinition("title",
                StringType.DEFAULT, "The title of the announcement.", null, 1,
                1));
        title.setValue(a.title());
        add(title);
        Field<String> text = new Field<String>(new FieldDefinition("text",
                TextType.DEFAULT, "The text content of the announcement.",
                null, 1, 1));
        text.setValue(a.text());
        add(text);
        Field<Date> created = new Field<Date>(new FieldDefinition("created",
                DateType.DATE_AND_TIME,
                "The date (and time) when the announcement is made.", null, 1,
                1));
        created.setValue(a.created());
        add(created);
        if (a.expiry() != null) {
            Field<Date> expiry = new Field<Date>(
                    new FieldDefinition(
                            "expiry",
                            DateType.DATE_AND_TIME,
                            "The expected date (and time) when the announcement will expire.",
                            null, 1, 1));
            expiry.setValue(a.created());
            add(expiry);
        }
    }
}
