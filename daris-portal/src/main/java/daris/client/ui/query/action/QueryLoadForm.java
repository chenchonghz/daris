package daris.client.ui.query.action;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.Query;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.QueryAssetRef;
import daris.client.ui.query.SavedQueryNavigator;

public class QueryLoadForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private SavedQueryNavigator _nav;
    private QueryAssetRef _selected;
    private ObjectResolveHandler<Query> _rh;

    public QueryLoadForm(ObjectResolveHandler<Query> rh) {
        _nav = new SavedQueryNavigator();
        _nav.fitToParent();
        _nav.setSelectionHandler(new SelectionHandler<QueryAssetRef>() {

            @Override
            public void selected(QueryAssetRef o) {
                _selected = o;
                notifyOfChangeInState();
            }

            @Override
            public void deselected(QueryAssetRef o) {
                _selected = null;
                notifyOfChangeInState();
            }
        });
        _rh = rh;
    }

    public QueryLoadForm() {
        this(null);
    }

    public void setLoadHandler(ObjectResolveHandler<Query> rh) {
        _rh = rh;
    }

    @Override
    public Widget gui() {
        return _nav;
    }

    @Override
    public Validity valid() {
        if (_selected == null) {
            return new Validity() {

                @Override
                public boolean valid() {
                    return false;
                }

                @Override
                public String reasonForIssue() {
                    return "No query is selected.";
                }
            };
        } else {
            return IsValid.INSTANCE;
        }
    }

    @Override
    public void execute(final ActionListener l) {
        if (_selected == null) {
            l.executed(false);
            return;
        }
        _selected.resolve(new ObjectResolveHandler<QueryAsset>() {

            @Override
            public void resolved(QueryAsset qa) {
                l.executed(qa != null);
                if (_rh != null) {
                    _rh.resolved(qa == null ? null : Query.create(qa));
                }
            }
        });
    }

}
