package daris.client.model.sc;

import com.google.gwt.user.client.Timer;

import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.file.DTIFile;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskCreateHandler;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlDocMaker;
import arc.mf.model.service.task.ServiceExecuteControls;
import arc.mf.model.service.task.ServiceExecuteTask;
import daris.client.model.sc.messages.ShoppingCartOutputRetrieve;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.widget.MessageBox;

public class DTIDownloadTask {

    public static ServiceExecuteTask execute(final ShoppingCart cart, final String dstDirPath, boolean decompress,
            boolean overwrite, final ActionListener al) {

        String dstFileName = ShoppingCartOutputRetrieve.generateArchiveFileName(cart);
        DTIDirectory dstDir = new DTIDirectory(dstDirPath);
        DTIFile dstFile = new DTIFile(dstDirPath + '/' + dstFileName);
        String name = "Download shopping cart " + cart.id();
        String desc = name + " via Arcitecta Desktop Integration(DTI) applet.";
        ServiceExecuteControls sec = new ServiceExecuteControls();
        sec.setDecompress(decompress);
        sec.setOverwrite(overwrite);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", cart.id());
        ServiceExecuteTask task = ServiceExecuteTask.create(name, desc, ShoppingCartOutputRetrieve.SERVICE_NAME,
                dm.root(), null, decompress ? dstDir : dstFile, sec, new DTITaskCreateHandler<ServiceExecuteTask>() {

                    @Override
                    public void created(ServiceExecuteTask task) {
                        if (al != null) {
                            al.executed(true);
                        }
                        new DTITaskDialog(task, DObjectBrowser.get(false).window());
                        task.monitor(1000, false, new DTITaskStatusHandler<DTITask>() {

                            @Override
                            public void status(Timer t, DTITask task) {
                                if (task != null) {
                                    if (task.finished()) {
                                        MessageBox.display(
                                                MessageBox.Type.info,
                                                "Downloading shopping cart " + cart.id() + " via DTI(task_id="
                                                        + task.id() + ")", "Task status: "
                                                        + task.status().toString().toLowerCase(), 3);
                                    }
                                    if (task.status() == DTITask.State.COMPLETED) {
                                        MessageBox.display(
                                                MessageBox.Type.info,
                                                "Downloading shopping cart " + cart.id() + " via DTI(task_id="
                                                        + task.id() + ")", "Shopping cart " + cart.id()
                                                        + " has been download to " + dstDirPath + ".", 3);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void completed(ServiceExecuteTask task) {
                        if (al != null) {
                            al.executed(true);
                        }
                        MessageBox.display(MessageBox.Type.info, "Completed downloading shopping cart  " + cart.id(),
                                " via DTI.", 3);

                    }

                    @Override
                    public void failed() {
                        if (al != null) {
                            al.executed(false);
                        }
                        Dialog.inform("Error", "Failed to download shopping cart " + cart.id() + " via DTI.");
                    }
                });
        return task;
    }
}
