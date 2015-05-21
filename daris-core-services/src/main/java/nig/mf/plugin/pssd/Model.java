package nig.mf.plugin.pssd;

import nig.mf.plugin.pssd.method.ExMethod;

/**
 * The PSSD Object model.
 * 
 * @author Jason Lohrey
 * 
 */
public class Model {

    private static PSSDObject.Type[] ROOT = { PSSDObject.Type.project };
    private static PSSDObject.Type[] PROJECT = { PSSDObject.Type.subject };
    private static PSSDObject.Type[] SUBJECT = { PSSDObject.Type.ex_method };
    private static PSSDObject.Type[] EXMETHOD = { PSSDObject.Type.study };
    private static PSSDObject.Type[] STUDY = { PSSDObject.Type.dataset };
    private static PSSDObject.Type[] DATASET = { PSSDObject.Type.dataset, PSSDObject.Type.data_object };

    /**
     * The valid collection members for the specified type.
     * 
     * @param type
     * @return
     */
    public static PSSDObject.Type[] memberTypesFor(String type) {

        if (type == null) {
            return ROOT;
        }

        if (type.equals(Project.TYPE)) {
            return PROJECT;
        }

        if (type.equals(Subject.TYPE)) {
            return SUBJECT;
        }

        if (type.equals(ExMethod.TYPE)) {
            return EXMETHOD;
        }

        if (type.equals(Study.TYPE)) {
            return STUDY;
        }

        if (type.equals(DataSet.TYPE)) {
            return DATASET;
        }

        return null;
    }

    /**
     * Check that the model element of an asset indicates that the asset is a
     * PSSD model object
     * 
     * @param model
     * @return
     */
    public static Boolean isPSSDModel(String model) {
        if (model == null)
            return false; // No model
        return model.startsWith("om.pssd."); // We don't really need to check
                                             // the values precisely
    }

}
