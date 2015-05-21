package daris.client.ui.sc.sink;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sink.FileSystemSink;
import daris.client.model.sink.FileSystemSink.Save;

public class FileSystemSinkForm extends SinkForm<FileSystemSink> {

    FileSystemSinkForm(DeliveryDestination destination, FileSystemSink sink, FormEditMode mode) {
        super(destination, sink, mode);
    }

    @Override
    void fillInArgs(SimplePanel sp) {

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        Form form = new Form(mode());

        Field<String> directory = new Field<String>(new FieldDefinition(FileSystemSink.Param.DIRECTORY.paramName(),
                ConstantType.DEFAULT, "The root directory into which the data should be placed.", null, 1, 1));
        if (sink().directory() != null) {
            directory.setInitialValue(sink().directory(), false);
        }
        form.add(directory);

        DeliveryArg arg = destination().arg(FileSystemSink.Param.PATH.paramName());
        final Field<String> path = new Field<String>(new FieldDefinition(FileSystemSink.Param.PATH.paramName(),
                StringType.DEFAULT, "A file path relative to the root directory for the data.", null, 0, 1));
        if (sink().path() != null) {
            path.setInitialValue(sink().path(), false);
        }
        if (arg != null && arg.value() != null) {
            path.setInitialValue(arg.value(), false);
        }
        path.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                destination().setArg(FileSystemSink.Param.PATH.paramName(), DeliveryArg.Type.delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        form.add(path);

        arg = destination().arg(FileSystemSink.Param.SAVE.paramName());
        Field<FileSystemSink.Save> save = new Field<FileSystemSink.Save>(
                new FieldDefinition(
                        FileSystemSink.Param.SAVE.paramName(),
                        new EnumerationType<FileSystemSink.Save>(FileSystemSink.Save.values()),
                        "one of [meta,content,both] - indicates whether only metadata, or content or both should be saved. This argument is optional - defaults to 'content'.",
                        null, 0, 1));
        if (sink().save() != null) {
            save.setInitialValue(sink().save(), false);
        }
        if (arg != null && arg.value() != null) {
            save.setInitialValue(FileSystemSink.Save.fromString(arg.value()), false);
        }
        save.addListener(new FormItemListener<Save>() {

            @Override
            public void itemValueChanged(FormItem<Save> f) {
                destination().setArg(FileSystemSink.Param.SAVE.paramName(), DeliveryArg.Type.delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Save> f, Property property) {

            }
        });
        form.add(save);

        arg = destination().arg(FileSystemSink.Param.DECOMPRESS.paramName());
        Field<Boolean> decompress = new Field<Boolean>(new FieldDefinition(FileSystemSink.Param.DECOMPRESS.paramName(),
                BooleanType.DEFAULT_ONE_ZERO, "Decompress data if compressed? Defaults to 0.", null, 0, 1));
        if (sink().decompress()) {
            decompress.setInitialValue(sink().decompress(), false);
        }
        if (arg != null && arg.value() != null) {
            decompress.setInitialValue(Boolean.parseBoolean(arg.value()), false);
        }
        decompress.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                boolean decompress = f.value();
                path.setMandatory(!decompress);
                destination().setArg(FileSystemSink.Param.DECOMPRESS.paramName(), DeliveryArg.Type.delivery_arg,
                        decompress);
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {
            }
        });
        form.add(decompress);

        form.render();

        addMustBeValid(form);

        vp.add(form);

        sp.setContent(vp);
    }
}
