package daris.client.util;

import java.util.List;

import arc.mf.client.ServerClient.Input;
import arc.mf.client.ServerClient.Output;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlStringWriterNe;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.desktop.server.ServiceCallThreadPool;
import arc.mf.desktop.server.Session;
import arc.xml.XmlDoc;

public abstract class ServiceTask<T> implements Runnable {

    public static interface ResponseHandler<T> {
        void responded(T o, Output output);
    }

    private String _service;
    private Output _output;
    private List<Input> _inputs;
    private ResponseHandler<T> _rh;

    public ServiceTask(String service) {
        _service = service;
    }

    public ServiceTask<T> setInputs(List<Input> inputs) {
        _inputs = inputs;
        return this;
    }

    public ServiceTask<T> setOutput(Output output) {
        _output = output;
        return this;
    }

    public ServiceTask<T> setResponseHandler(ResponseHandler<T> rh) {
        _rh = rh;
        return this;
    }

    public abstract void setServiceArgs(XmlWriterNe w);

    public abstract T instantiate(XmlDoc.Element xe);

    @Override
    public void run() {
        XmlStringWriterNe w = new XmlStringWriterNe();
        setServiceArgs(w);
        XmlDoc.Element re;
        try {
            /*
             * execute the service
             */
            re = Session.connection().executeMultiInput(_service, w.document(),
                    _inputs, _output);

            /*
             * instantiate result object
             */
            T o = instantiate(re);
            /*
             * call response handler
             */
            if (_rh != null) {
                _rh.responded(o, _output);
            }
        } catch (Throwable e) {
            UnhandledException.report("Executing service " + _service, e);
        }

    }

    public void execute() {
        ServiceCallThreadPool.execute(this);
    }
}