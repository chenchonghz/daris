package daris.client.model.sc;

import java.util.ArrayList;
import java.util.List;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.sink.Sink;
import daris.client.model.sink.SinkRef;
import daris.client.model.sink.messages.SinkList;

public class DeliveryDestinationEnum implements DynamicEnumerationDataSource<DeliveryDestination> {

    public DeliveryDestinationEnum() {

    }

    @Override
    public boolean supportPrefix() {
        return false;
    }

    @Override
    public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
        if (value == null) {
            handler.exists(value, false);
            return;
        }
        if (!value.startsWith(Sink.URL_PREFIX)) {
            handler.exists(value, DeliveryDestination.DOWNLOAD_DESTINATION_NAME.equals(value));
            return;
        }
        final String sinkName = value.substring(5);
        new SinkList().send(new ObjectMessageResponse<List<SinkRef>>() {

            @Override
            public void responded(List<SinkRef> sinks) {
                boolean exists = false;
                if (sinks != null && !sinks.isEmpty()) {
                    for (SinkRef sink : sinks) {
                        if (sink.name().equals(sinkName)) {
                            exists = true;
                            break;
                        }
                    }
                }
                handler.exists(value, exists);
            }
        });
    }

    @Override
    public void retrieve(final String prefix, final long start, final long end,
            final DynamicEnumerationDataHandler<DeliveryDestination> handler) {
        new SinkList().send(new ObjectMessageResponse<List<SinkRef>>() {

            @Override
            public void responded(List<SinkRef> sinks) {
                List<Value<DeliveryDestination>> values = new ArrayList<Value<DeliveryDestination>>();
                values.add(new Value<DeliveryDestination>(DeliveryDestination.DOWNLOAD_DESTINATION_NAME,
                        DeliveryDestination.DOWNLOAD_DESTINATION_NAME, DeliveryDestination.BROWSER));
                if (sinks != null) {
                    for (SinkRef sink : sinks) {
                        values.add(new Value<DeliveryDestination>(sink.url(), sink.url(), new DeliveryDestination(
                                DeliveryMethod.deposit, sink, null)));
                    }
                }
                handler.process(start, end, values.size(), values);
            }
        });
    }
}
