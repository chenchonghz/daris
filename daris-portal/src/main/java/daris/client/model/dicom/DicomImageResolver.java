package daris.client.model.dicom;

import java.util.List;
import java.util.Vector;

import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomImageGet;

public class DicomImageResolver {

	public static interface ImageResolveListener {

		void imageResolved(int index, int frame, DicomImage image);
	}

	private String _assetId;
	private int _index;
	private int _frame;
	private List<ImageResolveListener> _listeners;

	public DicomImageResolver(String assetId) {

		_assetId = assetId;
		_index = 0;
		_frame = 0;
	}

	public void resolve(int index, int frame) {

		_index = index;
		_frame = frame;
		new DicomImageGet(_assetId, _index, _frame).send(new ObjectMessageResponse<DicomImage>() {

			@Override
			public void responded(DicomImage di) {
				
				notifyOfImageResolve(_index, _frame, di);
			}
		});
	}

	private void notifyOfImageResolve(int index, int frame, DicomImage di) {

		if (_listeners != null) {
			for (ImageResolveListener l : _listeners) {
				l.imageResolved(index, frame, di);
			}
		}
	}

	public void addResolveListener(ImageResolveListener l) {

		if (l == null) {
			return;
		}
		if (_listeners == null) {
			_listeners = new Vector<ImageResolveListener>();
		}
		_listeners.add(l);
	}

	public void removeResolveListener(ImageResolveListener l) {

		if (_listeners != null) {
			_listeners.remove(l);
		}
	}
}
