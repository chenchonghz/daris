package daris.client.model.sc;

import java.util.List;

import arc.mf.client.dti.DTI;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ListUtil;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.shopping.events.ShoppingCartEvent;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.cookies.ShoppingCartCookies;
import daris.client.model.sc.messages.ShoppingCartExists;
import daris.client.model.sc.messages.ShoppingCartOutputRetrieve;

/**
 * Listening to shopping cart (update) event, if the cart is in "data ready"
 * state and the delivery method is "download", start browser downloading.
 * 
 * @author wilson
 * 
 */
public class ShoppingCartDownloadManager {

    private static Subscriber _subscriber = null;

    private static boolean _useDTI = false;

    private static ShoppingCartDownloadHandler DEFAULT_DOWNLOAD_HANDLER = new ShoppingCartDownloadHandler() {
        @Override
        public void download(ShoppingCartRef sc, final ActionListener al) {
            if (DTI.enabled() && ShoppingCartCookies.useDTI() && ShoppingCartCookies.dtiDstDir() != null) {
                sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                    @Override
                    public void resolved(ShoppingCart cart) {
                        DTIDownloadTask.execute(cart, ShoppingCartCookies.dtiDstDir(),
                                ShoppingCartCookies.dtiDecompress(), ShoppingCartCookies.dtiOverwrite(), al);
                    }
                });
            } else {
                new ShoppingCartOutputRetrieve(sc).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        if (al != null) {
                            al.executed(true);
                        }
                    }
                });
            }
        }
    };

    private static ShoppingCartDownloadHandler _handler = DEFAULT_DOWNLOAD_HANDLER;

    public static boolean useDTI() {
        return _useDTI;
    }

    public static void setUseDTI(boolean useDTI) {
        _useDTI = useDTI;
    }

    public static ShoppingCartDownloadHandler downloadHandler() {
        return _handler;
    }

    public static void setDownloadHandler(ShoppingCartDownloadHandler handler) {
        _handler = handler;
    }

    /**
     * Subscribe to system event channel to listening to the shopping cart
     * event.
     */
    public static void subscribe() {
        if (_handler == null) {
            _handler = DEFAULT_DOWNLOAD_HANDLER;
        }
        if (_subscriber == null) {
            _subscriber = new Subscriber() {
                @Override
                public List<Filter> systemEventFilters() {
                    return ListUtil.list(new Filter(ShoppingCartEvent.SYSTEM_EVENT_NAME));
                }

                @Override
                public void process(SystemEvent se) {
                    final ShoppingCartRef sc = new ShoppingCartRef(((ShoppingCartEvent) se).cartId());
                    new ShoppingCartExists(sc).send(new ObjectMessageResponse<Boolean>() {

                        @Override
                        public void responded(Boolean exists) {
                            if (exists) {
                                sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                                    @Override
                                    public void resolved(ShoppingCart cart) {
                                        if (cart.status() == Status.data_ready
                                                && cart.destination().method() == DeliveryMethod.download) {
                                            download(sc, null);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            };
            SystemEventChannel.add(_subscriber);
        }
    }

    /**
     * Unsubscribe from the system event channel to stop listening the shopping
     * cart event.
     */
    public static void unsubscribe() {
        if (_subscriber != null) {
            SystemEventChannel.remove(_subscriber);
            _subscriber = null;
        }
    }

    public static void download(ShoppingCartRef sc, final ActionListener al) {
        if (_handler != null) {
            _handler.download(sc, al);
        } else {
            new ShoppingCartOutputRetrieve(sc).send(new ObjectMessageResponse<Null>() {

                @Override
                public void responded(Null r) {
                    if (al != null) {
                        al.executed(true);
                    }
                }
            });
        }
    }

}
