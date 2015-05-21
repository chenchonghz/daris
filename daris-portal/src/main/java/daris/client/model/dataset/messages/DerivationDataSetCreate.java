package daris.client.model.dataset.messages;

import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;

public class DerivationDataSetCreate extends DObjectCreate {

	public DerivationDataSetCreate(DObjectRef po, DerivedDataSet o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.derivation.create";
	}

}
