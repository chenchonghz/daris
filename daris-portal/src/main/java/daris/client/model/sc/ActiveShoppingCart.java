package daris.client.model.sc;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.Action;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.messages.ShoppingCartContentAdd;
import daris.client.model.sc.messages.ShoppingCartContentClear;
import daris.client.model.sc.messages.ShoppingCartContentRefresh;
import daris.client.model.sc.messages.ShoppingCartContentRemove;
import daris.client.model.sc.messages.ShoppingCartCreate;
import daris.client.model.sc.messages.ShoppingCartList;
import daris.client.model.sc.messages.ShoppingCartOrder;

public class ActiveShoppingCart {

    public static interface Listener {
        void activeCartChanged(ShoppingCartRef asc);

        void contentChanged(ShoppingCartRef asc);
    }

    private static ShoppingCartRef _asc;
    private static List<Listener> _ls;

    public static void initialize() {
        _asc = null;
        if (_ls != null) {
            _ls.clear();
        }
    }

    public static void addListener(Listener l) {
        if (_ls == null) {
            _ls = new ArrayList<Listener>();
        }
        _ls.add(l);
    }

    public static void removeListener(Listener l) {
        if (_ls == null) {
            return;
        }
        _ls.remove(l);
    }

    private static void notifyOfCartChange(ShoppingCartRef asc) {
        if (_ls != null) {
            for (Listener l : _ls) {
                l.activeCartChanged(asc);
            }
        }
    }

    private static void notifyOfContentChange(ShoppingCartRef asc) {
        if (_ls != null) {
            for (Listener l : _ls) {
                l.contentChanged(asc);
            }
        }
    }

    public static void set(ShoppingCartRef cart) {
        set(cart, true);
    }

    public static void set(ShoppingCartRef cart, boolean fireEvent) {

        if (!ObjectUtil.equals(_asc, cart)) {
            _asc = cart;
            if (fireEvent) {
                notifyOfCartChange(_asc);
            }
        }
    }

    public static void reset() {
        get(null, true);
    }

    public static void resolve(ObjectResolveHandler<ShoppingCart> rh) {
        resolve(rh, false);
    }

    public static void resolve(final ObjectResolveHandler<ShoppingCart> rh, final boolean reset) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(ShoppingCartRef asc) {
                if (reset) {
                    asc.reset();
                }
                if (rh != null) {
                    asc.resolve(rh);
                }
            }
        });
    }

    public static void get(ObjectResolveHandler<ShoppingCartRef> rh) {
        get(rh, false);
    }

    private static void get(final ObjectResolveHandler<ShoppingCartRef> rh, boolean reset) {
        if (reset) {
            set(null);
        }
        if (_asc != null) {
            if (rh != null) {
                rh.resolved(_asc);
            }
            return;
        }
        new ShoppingCartList(Status.editable).send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
            @Override
            public void responded(List<ShoppingCartRef> carts) {
                if (carts == null || carts.isEmpty()) {
                    new ShoppingCartCreate().send(new ObjectMessageResponse<ShoppingCartRef>() {
                        @Override
                        public void responded(ShoppingCartRef cart) {
                            set(cart);
                            if (rh != null) {
                                rh.resolved(_asc);
                            }
                        }
                    });
                } else {
                    set(carts.get(0));
                    if (rh != null) {
                        rh.resolved(_asc);
                    }
                }
            }
        });
    }

    public static void addContents(final String where, final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentAdd(asc, where).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void addContents(final DObjectRef o, final boolean recursive, final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentAdd(asc, o, recursive).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void addContents(final List<Long> assetIds, final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentAdd(asc, assetIds).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void addContents(final List<String> cids, final boolean recursive, final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentAdd(asc, cids, recursive).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void removeContents(final List<ContentItem> items, final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentRemove(asc, items).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void clearContents(final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentClear(asc).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void refreshContents(final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartContentRefresh(asc).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        notifyOfContentChange(asc);
                        if (callback != null) {
                            callback.execute();
                        }
                    }
                });
            }
        });
    }

    public static void order(final Action callback) {
        get(new ObjectResolveHandler<ShoppingCartRef>() {

            @Override
            public void resolved(final ShoppingCartRef asc) {
                new ShoppingCartList(Status.editable).send(new ObjectMessageResponse<List<ShoppingCartRef>>() {

                    @Override
                    public void responded(List<ShoppingCartRef> editableCarts) {
                        if (editableCarts != null && editableCarts.size() > 2) {
                            for (ShoppingCartRef editableCart : editableCarts) {
                                if (!asc.equals(editableCart)) {
                                    // TODO:
                                    // save sink settings;
                                    // save cart settings;
                                    new ShoppingCartOrder(asc).send(new ObjectMessageResponse<Null>() {

                                        @Override
                                        public void responded(Null r) {
                                            if (callback != null) {
                                                callback.execute();
                                            }
                                        }
                                    });
                                    set(editableCart);
                                    return;
                                }
                            }
                        }
                        new ShoppingCartCreate().send(new ObjectMessageResponse<ShoppingCartRef>() {

                            @Override
                            public void responded(ShoppingCartRef cart) {

                                // TODO:
                                // save sink settings;
                                // save cart settings;
                                new ShoppingCartOrder(asc).send(new ObjectMessageResponse<Null>() {

                                    @Override
                                    public void responded(Null r) {
                                        if (callback != null) {
                                            callback.execute();
                                        }
                                    }
                                });
                                set(cart);
                            }
                        });
                    }
                });
            }
        });
    }

    public static boolean isActive(long cartId) {
        if (_asc == null) {
            return false;
        }
        return _asc.id() == cartId;
    }
}
