package daris.client.ui.object.action;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormItem.XmlType;
import arc.mf.client.file.LocalFile;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.task.DerivedDataSetCreateTask;

public class DerivedDataSetCreateForm extends DataSetCreateForm {
    
    public DerivedDataSetCreateForm(DObjectRef po, DObjectRef input,
            List<LocalFile> files) {
        super(new DerivedDataSetCreateTask(po, input, files), po);
    }

    @Override
    protected void addInterfaceFormItems(Form interfaceForm) {

        Field<Boolean> processedField = new Field<Boolean>(new FieldDefinition(
                "processed", BooleanType.DEFAULT_TRUE_FALSE,
                "Indicates whether the data is processed.", null, 1, 1));
        processedField.setValue(false);
        interfaceForm.add(processedField);

       
        
        FieldGroup inputFieldGroup = new FieldGroup(
                new FieldDefinition(
                        "input",
                        DocType.DEFAULT,
                        "Input data set(s) from which the derivation was made, if available.",
                        null, 0, Integer.MAX_VALUE));
        final Field<String> inputVidField = new Field<String>(new FieldDefinition(
                "vid", StringType.DEFAULT,
                "The value identifier for the data set.", null, 1, 1));
        inputVidField.setXmlType(XmlType.ATTRIBUTE);
        inputFieldGroup.add(inputVidField);
        Field<String> inputIdField = new Field<String>(new FieldDefinition(
                null, StringType.DEFAULT, "The citeable id of the input data set.",
                null, 1, 1));
        inputFieldGroup.add(inputIdField);
        
        DerivedDataSetCreateTask task = (DerivedDataSetCreateTask)task();
        DObjectRef input = task.inputDataSet();
        if(input!=null){
            inputIdField.setValue(input.id());
            if(input.referent()!=null&&input.referent().vid()==null){
                input.reset();
            }
            input.resolve(new ObjectResolveHandler<DObject>(){

                @Override
                public void resolved(DObject o) {
                   if(o!=null){
                       inputVidField.setValue(o.vid());
                   }                    
                }});
        }
        interfaceForm.add(inputFieldGroup);

    }
}
