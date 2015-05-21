package daris.client.model.sc;

import arc.mf.client.util.ActionListener;

public interface ShoppingCartDownloadHandler {

    public abstract void download(ShoppingCartRef sc, ActionListener al);

}
