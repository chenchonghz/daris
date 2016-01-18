package daris.client.model.dictionary;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.TermRef;
import daris.client.model.dictionary.messages.DictionaryEntryExists;

public class DictionaryEntryEnumerationDataSource
        implements DynamicEnumerationDataSource<TermRef> {

    private DictionaryEntryCollectionRef _entries;

    public DictionaryEntryEnumerationDataSource(String dictionary) {
        _entries = new DictionaryEntryCollectionRef(dictionary);
    }

    public DictionaryRef dictionary() {
        return _entries.dictionary();
    }

    public String dictionaryName() {
        return _entries.dictionaryName();
    }

    @Override
    public void exists(String value, DynamicEnumerationExistsHandler handler)
            throws Throwable {
        if (value == null || value.trim().equals("")) {
            handler.exists(value, false);
            return;
        }
        new DictionaryEntryExists(dictionaryName(), value).send(exists -> {
            handler.exists(value, exists);
        });
    }

    @Override
    public void retrieve(String prefix, long start, long end,
            DynamicEnumerationDataHandler<TermRef> dh) throws Throwable {
        _entries.reset();
        _entries.setPrefix(prefix);
        _entries.resolve(start, end, terms -> {
            if (terms != null && !terms.isEmpty()) {
                List<Value<TermRef>> values = new ArrayList<Value<TermRef>>(
                        terms.size());
                for (TermRef term : terms) {
                    values.add(
                            new Value<TermRef>(term.term(), term.term(), term));
                }
                dh.process(start, end, values.size(), values);
                return;
            }
            dh.process(0, 0, 0, null);
        });
    }

    @Override
    public boolean supportPrefix() {
        return true;
    }

}
