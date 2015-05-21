package daris.client.model.query.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.Action;
import arc.mf.client.util.ListUtil;
import arc.mf.model.asset.AssetRef;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.ResultCollectionRef;
import daris.client.model.sc.ActiveShoppingCart;

public class QueryResult {

    public static <T extends ObjectRef<?>> void addToShoppingCart(ResultCollectionRef<T> rc, final Action postAction)
            throws Throwable {
        ActiveShoppingCart.addContents(rc.query().filter().toString(), postAction);
    }

    public static <T extends ObjectRef<?>> void addToShoppingCart(List<T> objects, final Action postAction) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        if (objects.get(0) instanceof AssetRef) {
            List<Long> ids = new ArrayList<Long>(objects.size());
            for (Object o : objects) {
                ids.add(((AssetRef) o).id());
            }
            ActiveShoppingCart.addContents(ids, postAction);
        }
        if (objects.get(0) instanceof DObjectRef) {
            List<String> cids = new ArrayList<String>(objects.size());
            for (Object o : objects) {
                cids.add(((DObjectRef) o).id());
            }
            ActiveShoppingCart.addContents(cids, true, postAction);
        }
    }

    public static <T extends ObjectRef<?>> void addToShoppingCart(T o, final Action postAction) {
        if (o instanceof AssetRef) {
            ActiveShoppingCart.addContents(ListUtil.list(((AssetRef) o).id()), postAction);
        }
        if (o instanceof DObjectRef) {
            ActiveShoppingCart.addContents((DObjectRef) o, true, postAction);
        }
    }

    public static <T extends ObjectRef<?>> void export(ResultCollectionRef<T> rc, long idx, int size,
            QueryResultExport.Format fileFormat, String fileName, final Action postAction) {
        new QueryResultExport(rc, idx, size, fileFormat, fileName).send(new ObjectMessageResponse<Null>() {

            @Override
            public void responded(Null r) {
                if (postAction != null) {
                    postAction.execute();
                }
            }
        });
    }

    public static <T extends ObjectRef<?>> void export(ResultCollectionRef<T> rc, QueryResultExport.Format fileFormat,
            String fileName, final Action postAction) {
        new QueryResultExport(rc, fileFormat, fileName).send(new ObjectMessageResponse<Null>() {

            @Override
            public void responded(Null r) {
                if (postAction != null) {
                    postAction.execute();
                }
            }
        });
    }
}
