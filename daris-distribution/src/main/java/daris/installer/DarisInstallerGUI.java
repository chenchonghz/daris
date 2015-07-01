package daris.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import arc.mf.client.ClientTask;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.mf.client.gui.LocalProperties;
import arc.mf.client.gui.LoginDialog;
import arc.mf.client.gui.Session;
import arc.utils.ProgressMonitor;
import daris.installer.DarisInstaller.PackageEntry;

public class DarisInstallerGUI implements PropertyChangeListener {

	public static final String PROPERTIES_DIR = System.getProperty("user.home")
			+ "/.daris";

	public static final String PROPERTIES_FILE = PROPERTIES_DIR
			+ "/daris-installer.xml";

	private JFrame _frame;

	private JPanel _containerPane;
	private Box _centerPane;
	private JTable _pkgTable;
	private JPanel _progressPane;
	private JProgressBar _progressBar;
	private JTextArea _progressTextArea;
	private JButton _installButton;

	private Map<PackageEntry.Type, PackageEntry> _pkgEntries;
	private InstallTask _task;

	public DarisInstallerGUI(Set<PackageEntry.Type> pkgTypes) throws Throwable {

		/*
		 * create frame
		 */
		_frame = new JFrame("Install DaRIS packages...");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setLocationRelativeTo(null);

		/*
		 * initialize session
		 */
		while (!Session.connected()) {
			LocalProperties.setLocation(new File(PROPERTIES_FILE));
			LoginDialog loginDialog = new LoginDialog(
					LoginDialog.TYPE_DOMAIN_USER, _frame, "Log in Mediaflux",
					null,
					"Log into the Mediaflux server that you want to install DaRIS to.");
			loginDialog.loadSettings();
			LoginDialog.setDefaultDialog(loginDialog);
			boolean connected = Session.connect();
			if (!connected) {
				if (loginDialog.cancelled()) {
					System.exit(0);
				}
			}
		}

		/*
		 * update title
		 */
		_frame.setTitle(new StringBuilder("Install DaRIS Packages (")
				.append(Session.domain()).append(":").append(Session.user())
				.append("@").append(Session.host()).append(")").toString());

		/*
		 * 
		 */
		_containerPane = new JPanel(new BorderLayout());
		_containerPane.setBorder(BorderFactory
				.createEmptyBorder(10, 20, 10, 20));

		_centerPane = Box.createVerticalBox();
		_containerPane.add(_centerPane, BorderLayout.CENTER);

		/*
		 * package selections
		 */
		JPanel pkgSelectPane = new JPanel(new BorderLayout());
		JLabel pkgSelectLabel = new JLabel("Select DaRIS packages to install:");
		pkgSelectPane.add(pkgSelectLabel, BorderLayout.PAGE_START);

		_pkgEntries = DarisInstaller.getPackageEntries();
		pkgTypes = pkgTypes == null ? new TreeSet<PackageEntry.Type>()
				: pkgTypes;
		if (pkgTypes.isEmpty()) {
			pkgTypes.add(PackageEntry.Type.ESSENTIALS);
			pkgTypes.add(PackageEntry.Type.CORE_SERVICES);
		}

		Object[][] pkgTableData = new Object[6][3];
		PackageEntry.Type[] types = PackageEntry.Type.values();
		for (int i = 0; i < types.length; i++) {
			PackageEntry.Type type = types[i];
			PackageEntry entry = _pkgEntries.get(type);
			pkgTableData[i] = new Object[] { pkgTypes.contains(type), type,
					entry.version() };
		}
		@SuppressWarnings("serial")
		DefaultTableModel tableModel = new DefaultTableModel(pkgTableData,
				new String[] { "", "Package", "Version" }) {
			@Override
			public Class<?> getColumnClass(int col) {
				if (col == 0)
					return Boolean.class;
				return super.getColumnClass(col);
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				if (col == 0) {
					return true;
				}
				return false;
			}
		};
		tableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
					_installButton.setEnabled(getTotalSelected() > 0);
				}
			}
		});
		_pkgTable = new JTable(tableModel);
		_pkgTable.getColumnModel().getColumn(0).setMaxWidth(20);
		_pkgTable.getColumnModel().getColumn(1).setPreferredWidth(180);
		_pkgTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		_pkgTable.setShowGrid(true);
		_pkgTable.setGridColor(Color.LIGHT_GRAY);
		pkgSelectPane.add(new JScrollPane(_pkgTable), BorderLayout.CENTER);

		pkgSelectPane.setPreferredSize(new Dimension(Short.MAX_VALUE, 200));
		pkgSelectPane.setMinimumSize(new Dimension(Short.MAX_VALUE, 200));
		pkgSelectPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
		_centerPane.add(pkgSelectPane);

		/*
		 * progress
		 */
		_progressPane = new JPanel(new BorderLayout());

		_progressBar = new JProgressBar(0, 100);
		_progressBar.setStringPainted(true);
		_progressPane.add(_progressBar, BorderLayout.PAGE_START);

		_progressTextArea = new JTextArea();
		_progressTextArea.setEditable(false);
		_progressPane.add(new JScrollPane(_progressTextArea),
				BorderLayout.CENTER);

		_progressPane.setMaximumSize(new Dimension(Short.MAX_VALUE,
				Short.MAX_VALUE));
		_centerPane.add(_progressPane);

		/*
		 * buttons
		 */
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		_installButton = new JButton("Install");
		_installButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ("Install".equalsIgnoreCase(_installButton.getText())) {
					_pkgTable.setEnabled(false);
					_installButton.setText("Abort");
					try {
						Session.execute(_frame, new ClientTask<Void>() {

							@Override
							public Void execute(Connection cxn)
									throws Throwable {
								_task = new InstallTask(DarisInstallerGUI.this,
										cxn);
								_task.addPropertyChangeListener(DarisInstallerGUI.this);
								_task.execute();
								return null;
							}
						});
					} catch (Throwable ex) {
						appendProgressMessage("Error: " + ex.getMessage(), true);
						_installButton.setText("Install");
					}
				} else if ("Abort".equalsIgnoreCase(_installButton.getText())) {
					_installButton.setEnabled(false);
					if (_task != null) {
						_task.cancel(true);
					}
				} else if ("Complete".equalsIgnoreCase(_installButton.getText())) {
					System.exit(0);
				}
			}
		});
		buttonPane.add(_installButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));

		_containerPane.add(buttonPane, BorderLayout.PAGE_END);

		/*
		 * show
		 */
		_frame.setContentPane(_containerPane);
		_frame.revalidate();

	}

	private boolean isSelected(PackageEntry.Type pkgType) {
		return (Boolean) _pkgTable.getValueAt(pkgType.ordinal(), 0);
	}

	private int getTotalSelected() {
		int nbRows = _pkgTable.getRowCount();
		int selected = 0;
		for (int i = 0; i < nbRows; i++) {
			if ((Boolean) _pkgTable.getValueAt(i, 0)) {
				selected++;
			}
		}
		return selected;
	}

	public void show() {
		_frame.setSize(640, 480);
		_frame.setLocationRelativeTo(null);
		_frame.setVisible(true);
	}

	public static void start(final Set<PackageEntry.Type> pkgTypes)
			throws Throwable {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new DarisInstallerGUI(pkgTypes).show();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) throws Throwable {
		start(null);
	}

	private void appendProgressMessage(String message, boolean lineBreak) {
		_progressTextArea.append(message);
		if (lineBreak) {
			_progressTextArea.append("\n");
		}
	}

	private static class InstallTask extends SwingWorker<Integer, String> {

		private DarisInstallerGUI _gui;
		private ServerClient.Connection _cxn;

		InstallTask(DarisInstallerGUI gui, ServerClient.Connection cxn) {
			_gui = gui;
			_cxn = cxn;
		}

		@Override
		public Integer doInBackground() throws Exception {
			int installed = 0;
			int totalSelected = _gui.getTotalSelected();
			String jarFilePath = DarisInstaller.getJarFilePath();
			PackageEntry.Type[] types = PackageEntry.Type.values();
			for (int i = 0; i < types.length; i++) {
				if (isCancelled()) {
					break;
				}
				PackageEntry.Type pkgType = types[i];
				if (!_gui.isSelected(pkgType)) {
					continue;
				}
				final PackageEntry pkgEntry = _gui._pkgEntries.get(pkgType);
				if (pkgEntry == null) {
					throw new Exception("Could not find daris package: "
							+ pkgType.fullName() + " in " + jarFilePath);
				}
				_cxn.setProgressMonitor(new ProgressMonitor() {
					@Override
					public boolean abort() {
						return isCancelled();
					}

					@Override
					public void begin(int i, long l) {
						publish("Installing " + pkgEntry.name() + "... ");
					}

					@Override
					public void beginMultiPart(int a, long b) {

					}

					@Override
					public void end(int i) {
						publish("done.\n");
					}

					@Override
					public void endMultiPart(int a) {

					}

					@Override
					public void update(long l) throws Throwable {
					}
				});
				try {
					DarisInstaller.installPackage(_cxn, pkgEntry);
				} catch (Throwable e) {
					if (e instanceof Exception) {
						throw (Exception) e;
					} else {
						throw new Exception(e);
					}
				}
				installed++;
				int progress = (int) (((double) installed / (double) totalSelected) * 100.0);
				setProgress(Math.min(progress, 100));
			}
			return installed;
		}

		@Override
		protected void process(List<String> msgs) {
			for (String msg : msgs) {
				_gui.appendProgressMessage(msg, false);
			}
		}

		@Override
		public void done() {
			try {
				get();
				_gui.completed();
			} catch (CancellationException | InterruptedException e) {
				_gui.aborted();
			} catch (ExecutionException e) {
				_gui.failed(e);
			}
		}
	}

	private void completed() {
		appendProgressMessage("Complete!", true);
		_installButton.setText("Complete");
		_installButton.setEnabled(true);
	}

	private void aborted() {
		appendProgressMessage("Aborted!", true);
		_installButton.setText("Install");
		_installButton.setEnabled(true);
		_pkgTable.setEnabled(true);
	}

	private void failed(Exception e) {
		appendProgressMessage("Error: " + e.getMessage(), true);
		StringWriter w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		appendProgressMessage(w.toString(), true);
		_installButton.setText("Install");
		_installButton.setEnabled(true);
		_pkgTable.setEnabled(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			_progressBar.setValue(progress);
		}
		// else if ("state".equals(evt.getPropertyName())
		// && SwingWorker.StateValue.DONE == evt.getNewValue()) {
		// if (_task != null && _task.isCancelled()) {
		// aborted();
		// } else {
		// completed();
		// }
		// }

	}

}
