package daris.client.model.secure.wallet;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;

public class SecureWalletEntryEnumerationDataSource implements DynamicEnumerationDataSource<SecureWalletEntryRef> {

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (value == null || value.trim().isEmpty()) {
            handler.exists(value, false);
            return;
        }
        SecureWallet.listEntries(new ObjectMessageResponse<List<SecureWalletEntryRef>>() {

            @Override
            public void responded(List<SecureWalletEntryRef> entries) {
                if (entries != null) {
                    for (SecureWalletEntryRef entry : entries) {
                        if (entry.key().equals(value)) {
                            handler.exists(value, true);
                            return;
                        }
                    }
                }
                handler.exists(value, false);
            }
        });
    }

    @Override
    public void retrieve(String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<SecureWalletEntryRef> handler) {
        SecureWallet.listEntries(new ObjectMessageResponse<List<SecureWalletEntryRef>>() {

            @Override
            public void responded(List<SecureWalletEntryRef> entries) {
                if (entries != null && !entries.isEmpty()) {
                    List<Value<SecureWalletEntryRef>> values = new ArrayList<Value<SecureWalletEntryRef>>();
                    for (SecureWalletEntryRef entry : entries) {
                        values.add(new Value<SecureWalletEntryRef>(entry.key(), "secure wallet entry, key: "
                                + entry.key(), entry));
                    }
                    List<Value<SecureWalletEntryRef>> rvs = values;
                    int total = values.size();
                    int start1 = (int) start;
                    start1 = start1 < 0 ? 0 : start1;
                    int end1 = (int) end;
                    end1 = end1 > total ? total : end1;
                    if (start1 < total) {
                        rvs = values.subList(start1, end1);
                    } else {
                        rvs = null;
                    }
                    handler.process(start1, end1, total, rvs);
                } else {
                    handler.process(0, 0, 0, null);
                }

            }
        });

    }

}
