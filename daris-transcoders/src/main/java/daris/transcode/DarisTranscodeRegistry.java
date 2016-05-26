package daris.transcode;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import daris.transcode.dcmtools.DCMToolsTranscodeProvider;
import daris.transcode.debabeler.DebabelerTranscodeProvider;
import daris.transcode.minctools.DCM2MNCTranscodeProvider;
import daris.transcode.mricron.MricronTranscodeProvider;
import daris.transcode.mrtrix.MRConvertTranscodeProvider;
import daris.transcode.pvconv.PVCONVTranscodeProvider;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderImpl;
import arc.mime.MimeType;

public class DarisTranscodeRegistry {

    private DarisTranscodeRegistry() {
    }

    private static class TranscoderBridge implements TranscoderImpl {

        private String _from;
        private String _to;

        TranscoderBridge(String from, String to) {
            _from = from;
            _to = to;
        }

        @Override
        public String description() {
            DarisTranscodeImpl impl = DarisTranscodeRegistry
                    .getActiveTranscoderImpl(_from, _to);
            if (impl == null) {
                return null;
            } else {
                return impl.description();
            }
        }

        @Override
        public String outputType(MimeType fromMimeType, MimeType toMimeType) {
            DarisTranscodeImpl impl = DarisTranscodeRegistry
                    .getActiveTranscoderImpl(_from, _to);
            if (impl == null) {
                return null;
            } else {
                return impl.outputType(fromMimeType, toMimeType);
            }
        }

        @Override
        public String transcode(File inputFile, MimeType fromType,
                MimeType fromContentType, MimeType toType, File outputFile,
                Map<String, String> params) throws Throwable {
            DarisTranscodeImpl impl = DarisTranscodeRegistry
                    .getActiveTranscoderImpl(_from, _to);
            if (impl == null) {
                return null;
            } else {
                return impl.transcode(inputFile, fromType, fromContentType,
                        toType, outputFile, params);
            }
        }

    }

    private static Map<String, DarisTranscodeImpl> _activeImpls;
    private static Map<String, DarisTranscodeProvider> _providers;

    private static String DELIMITER = ":";

    private static boolean _initialized = false;

    public static void initialize() {
        if (!_initialized) {
            DarisTranscodeRegistry.addProvider(
                    DebabelerTranscodeProvider.INSTANCE, false);
            DarisTranscodeRegistry.addProvider(
                    PVCONVTranscodeProvider.INSTANCE, false);
            DarisTranscodeRegistry.addProvider(
                    MRConvertTranscodeProvider.INSTANCE, false);
            DarisTranscodeRegistry.addProvider(
                    DCM2MNCTranscodeProvider.INSTANCE, true);
            DarisTranscodeRegistry.addProvider(
                    DCMToolsTranscodeProvider.INSTANCE, true);
            DarisTranscodeRegistry.addProvider(
                    MricronTranscodeProvider.INSTANCE, true);
            _initialized = true;
        }
    }

    public static void activate(DarisTranscodeImpl impl) {
        if (_activeImpls == null) {
            _activeImpls = new HashMap<String, DarisTranscodeImpl>();
        }
        _activeImpls.put(activeImplKey(impl), impl);
    }

    public static void activate(String provider) {
        DarisTranscodeProvider po = _providers == null ? null : _providers
                .get(provider);
        if (po != null) {
            Collection<DarisTranscodeImpl> impls = po.transcodeImpls();
            if (impls != null) {
                for (DarisTranscodeImpl impl : impls) {
                    activate(impl);
                }
            }
        }
    }

    public static void activate(String provider, String from, String to) {
        DarisTranscodeProvider po = _providers == null ? null : _providers
                .get(provider);
        if (po != null) {
            Collection<DarisTranscodeImpl> impls = po.transcodeImpls();
            if (impls != null) {
                for (DarisTranscodeImpl impl : impls) {
                    if (impl.from().equals(from) && impl.to().equals(to)) {
                        activate(impl);
                    }
                }
            }
        }
    }

    private static String activeImplKey(DarisTranscodeImpl impl) {
        return activeImplKey(impl.from(), impl.to());
    }

    private static String activeImplKey(String from, String to) {
        return from + DELIMITER + to;
    }

    public static void addProvider(DarisTranscodeProvider provider,
            boolean activate) {
        if (_providers == null) {
            _providers = new HashMap<String, DarisTranscodeProvider>();
        }
        _providers.put(provider.name(), provider);
        Collection<DarisTranscodeImpl> impls = provider.transcodeImpls();
        if (impls != null && !impls.isEmpty()) {
            if (_activeImpls == null) {
                _activeImpls = new HashMap<String, DarisTranscodeImpl>();
            }
            for (DarisTranscodeImpl impl : impls) {
                String key = activeImplKey(impl);
                if (!_activeImpls.containsKey(key) || activate) {
                    _activeImpls.put(key, impl);
                }
            }
        }
    }

    public static DarisTranscodeImpl getActiveTranscoderImpl(String from,
            String to) {
        if (_activeImpls == null) {
            return null;
        }
        return _activeImpls.get(activeImplKey(from, to));
    }

    public static DarisTranscodeProvider getActiveTranscoderProvider(
            String from, String to) {
        DarisTranscodeImpl impl = getActiveTranscoderImpl(from, to);
        if (impl == null) {
            return null;
        }
        return impl.provider();
    }

    public static String getActiveTranscoderProviderName(String from, String to) {
        DarisTranscodeProvider provider = getActiveTranscoderProvider(from, to);
        if (provider == null) {
            return null;
        }
        return provider.name();
    }

    public static Collection<DarisTranscodeImpl> getActiveTranscoderImpls() {
        if (_activeImpls == null) {
            return null;
        }
        return _activeImpls.values();
    }

    public static Collection<Transcoder> transcoders() {
        if (_activeImpls == null || _activeImpls.isEmpty()) {
            return null;
        }
        Collection<DarisTranscodeImpl> activeImpls = _activeImpls.values();
        Collection<Transcoder> transcoders = new Vector<Transcoder>();
        for (DarisTranscodeImpl impl : activeImpls) {
            transcoders.add(new Transcoder(impl.from(), impl.to(),
                    new TranscoderBridge(impl.from(), impl.to())));
        }
        return transcoders;
    }

    public static Set<DarisTranscodeProvider> providers() {
        if (_providers == null || _providers.isEmpty()) {
            return null;
        }
        Set<String> names = new TreeSet<String>();
        names.addAll(_providers.keySet());
        Set<DarisTranscodeProvider> providers = new LinkedHashSet<DarisTranscodeProvider>();
        for (String name : names) {
            providers.add(_providers.get(name));
        }
        return providers;
    }

    public static boolean hasProvider(String providerName) throws Throwable {
        if (providerName == null) {
            return false;
        }
        if (_providers == null || _providers.isEmpty()) {
            return false;
        }
        return _providers.containsKey(providerName);
    }

    public static DarisTranscodeProvider getProvider(String providerName)
            throws Throwable {
        if (providerName == null) {
            return null;
        }
        if (_providers == null || _providers.isEmpty()) {
            return null;
        }
        return _providers.get(providerName);
    }

    public static boolean hasTranscoder(String from, String to, String provider)
            throws Throwable {
        return getTranscoder(from, to, provider) != null;
    }

    public static DarisTranscodeImpl getTranscoder(String from, String to,
            String provider) throws Throwable {
        if (from == null || to == null) {
            return null;
        }
        if (_providers == null || _providers.isEmpty()) {
            return null;
        }
        if (provider == null) {
            for (DarisTranscodeProvider p : _providers.values()) {
                Collection<DarisTranscodeImpl> impls = p.transcodeImpls();
                if (impls == null || impls.isEmpty()) {
                    return null;
                }
                for (DarisTranscodeImpl impl : impls) {
                    if (from.equals(impl.from()) && to.equals(impl.to())) {
                        return impl;
                    }
                }
            }
        } else {
            DarisTranscodeProvider p = _providers.get(provider);
            if (p == null) {
                return null;
            }
            Collection<DarisTranscodeImpl> impls = p.transcodeImpls();
            if (impls == null || impls.isEmpty()) {
                return null;
            }
            for (DarisTranscodeImpl impl : impls) {
                if (from.equals(impl.from()) && to.equals(impl.to())) {
                    return impl;
                }
            }
        }
        return null;
    }
}
