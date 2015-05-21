package daris.client.model.task;

import java.util.List;
import java.util.Map;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.dataset.DataSet.Transform;
import daris.client.model.object.DObjectRef;

public class PrimaryDataSetCreateTask extends ImportTask {

    public static final String SERVICE = "om.pssd.dataset.primary.create";

    private static class Arguments implements Args {
        public String pid;
        public String proute;
        public String name;
        public String filename;
        public String description;
        public Transform transform;
        public XmlElement meta;

        public Arguments(XmlElement ae) throws Throwable {
            pid = ae.value("pid");
            proute = ae.value("pid/@proute");
            name = ae.value("name");
            filename = ae.value("filename");
            description = ae.value("description");
            XmlElement te = ae.element("transform");
            if (te != null) {
                transform = new Transform(te);
            } else {
                transform = null;
            }
            meta = ae.element("meta");
        }

        public Arguments(String pid, String proute, String name, String filename, String description,
                Transform transform, XmlElement meta) {
            this.pid = pid;
            this.proute = proute;
            this.name = name;
            this.description = description;
            this.transform = transform;
            this.meta = meta;
        }

        @Override
        public void save(XmlWriter w) {
            w.add("pid", new String[] { "proute", proute }, pid);
            if (name != null) {
                w.add("name", name);
            }
            if (filename != null) {
                w.add("filename", filename);
            }
            if (description != null) {
                w.add("description", description);
            }
            if (transform != null) {
                transform.describe(w);
            }
            if (meta != null) {
                w.add(meta, true);
            }
        }
    }

    public PrimaryDataSetCreateTask(DObjectRef po, List<LocalFile> files) {
        this(files, null);
        setParent(po.id(), po.proute());
    }

    protected PrimaryDataSetCreateTask(List<LocalFile> files, Map<String, String> variables) {
        super(FileCompilationProfile.PSSD_IMPORT, files, variables);
        setVariable(VAR_SERVICE, SERVICE);
    }

    @Override
    public void setFiles(List<LocalFile> files) {
        if (files != null && files.size() == 1) {
            String filename = files.get(0).name();
            Arguments args = (Arguments) args();
            if (args == null) {
                args = new Arguments(null, null, null, filename, null, null, null);
            }
            args.filename = filename;
            setArgs(args);
        }
        super.setFiles(files);
    }

    @Override
    protected Args parseArgs(XmlElement ae) throws Throwable {
        return new Arguments(ae);
    }

    public void setParent(String pid, String proute) {
        Arguments args = (Arguments) args();
        if (args == null) {
            args = new Arguments(pid, proute, null, null, null, null, null);
        }
        args.pid = pid;
        args.proute = proute;
        setArgs(args);
    }

    public void setName(String name) {
        Arguments args = (Arguments) args();
        if (args == null) {
            args = new Arguments(null, null, name, null, null, null, null);
        }
        args.name = name;
        setArgs(args);
    }

    public void setDescription(String description) {
        Arguments args = (Arguments) args();
        if (args == null) {
            args = new Arguments(null, null, null, null, description, null, null);
        }
        args.description = description;
        setArgs(args);
    }

    public void setTransform(Transform transform) {
        Arguments args = (Arguments) args();
        if (args == null) {
            args = new Arguments(null, null, null, null, null, transform, null);
        }
        args.transform = transform;
        setArgs(args);
    }

    public void setMeta(XmlElement meta) {
        Arguments args = (Arguments) args();
        if (args == null) {
            args = new Arguments(null, null, null, null, null, null, meta);
        }
        args.meta = meta;
        setArgs(args);
    }
}
