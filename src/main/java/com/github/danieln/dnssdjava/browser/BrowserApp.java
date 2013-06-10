/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.github.danieln.dnssdjava.DnsSDBrowser;
import com.github.danieln.dnssdjava.DnsSDDomainEnumerator;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import com.github.danieln.dnssdjava.ServiceType;

/**
 * Graphical DNS-SD browser.
 * @author Daniel Nilsson
 */
public class BrowserApp extends JFrame {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BrowserApp app = new BrowserApp();
				app.setDefaultCloseOperation(EXIT_ON_CLOSE);
				app.setLocationByPlatform(true);
				app.setVisible(true);
				app.computerDomainChanged();
			}
		});
	}

	private JTextField computerDomainField;
	private JComboBox browsingDomainCombo;
	private JList serviceTypeList;
	private JTextField subtypeField;
	private JList serviceInstanceList;
	private JTextField nameField;
	private JTextField hostField;
	private JTextField portField;
	private JTable propertiesTabel;
	private DnsSDBrowser serviceBrowser;

	public BrowserApp() {
		super("DNS-SD Browser");
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		Listener listener = new Listener();
		JLabel computerDomainLabel = new JLabel("Computer Domain:");
		computerDomainField = new JTextField(20);
		computerDomainField.addActionListener(listener);
		JLabel browsingDomainLabel = new JLabel("Browsing Domain:");
		browsingDomainCombo = new JComboBox();
		browsingDomainCombo.setEditable(true);
		browsingDomainCombo.addActionListener(listener);
		JLabel serviceTypeLabel = new JLabel("Service Types:");
		serviceTypeList = new JList();
		serviceTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceTypeList.addListSelectionListener(listener);
		JScrollPane serviceTypeScroll = new JScrollPane(serviceTypeList);
		JLabel subtypeLabel = new JLabel("Subtypes:");
		subtypeField = new JTextField(20);
		subtypeField.addActionListener(listener);
		JLabel serviceInstanceLabel = new JLabel("Service Instances:");
		serviceInstanceList = new JList();
		serviceInstanceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceInstanceList.addListSelectionListener(listener);
		serviceInstanceList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(((ServiceName) value).getName());
				return this;
			}
		});
		JScrollPane serviceInstanceScroll = new JScrollPane(serviceInstanceList);
		serviceInstanceScroll.setPreferredSize(new Dimension(200, 200));
		JLabel nameLabel = new JLabel("Name:");
		nameField = new JTextField(20);
		nameField.setEditable(false);
		JLabel hostLabel = new JLabel("Host:");
		hostField = new JTextField(15);
		hostField.setEditable(false);
		JLabel portLabel = new JLabel("Port:");
		portField = new JTextField(5);
		portField.setEditable(false);
		JLabel propertiesLabel = new JLabel("Properties:");
		propertiesTabel = new JTable();
		propertiesTabel.setPreferredScrollableViewportSize(new Dimension(200, 200));
		JScrollPane propertiesScroll = new JScrollPane(propertiesTabel);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(computerDomainLabel)
						.addComponent(computerDomainField)
						.addComponent(browsingDomainLabel)
						.addComponent(browsingDomainCombo)
						.addComponent(serviceTypeLabel)
						.addComponent(serviceTypeScroll)
						.addComponent(subtypeLabel)
						.addComponent(subtypeField))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup()
						.addComponent(serviceInstanceLabel)
						.addComponent(serviceInstanceScroll))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup()
						.addComponent(nameLabel)
						.addComponent(nameField)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup()
										.addComponent(hostLabel)
										.addComponent(hostField))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup()
										.addComponent(portLabel)
										.addComponent(portField)))
						.addComponent(propertiesLabel)
						.addComponent(propertiesScroll)));
		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(computerDomainLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(computerDomainField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(browsingDomainLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(browsingDomainCombo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(serviceTypeLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(serviceTypeScroll)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(subtypeLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(subtypeField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(serviceInstanceLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(serviceInstanceScroll))
				.addGroup(layout.createSequentialGroup()
						.addComponent(nameLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(nameField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createBaselineGroup(false, true)
								.addComponent(hostLabel)
								.addComponent(portLabel))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createBaselineGroup(false, true)
								.addComponent(hostField)
								.addComponent(portField))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(propertiesLabel)
						.addComponent(propertiesScroll)));
		pack();
	}

	private void computerDomainChanged() {
		new SwingWorker<Void, Void>() {
		
			private final String domain = computerDomainField.getText().trim();
			
			private Collection<String> domains;
			private String defDomain;

			@Override
			protected Void doInBackground() throws Exception {
				DnsSDDomainEnumerator de;
				if (domain.isEmpty()) {
					de = DnsSDFactory.getInstance().createDomainEnumerator();
				} else { 
					de = DnsSDFactory.getInstance().createDomainEnumerator(domain);
				}
				domains = de.getBrowsingDomains();
				defDomain = de.getDefaultBrowsingDomain();
				return null;
			}
			
			@Override
			protected void done() {
				browsingDomainCombo.setModel(new DefaultComboBoxModel(domains.toArray(new String[domains.size()])));
				if (defDomain != null) {
					browsingDomainCombo.setSelectedItem(defDomain);
				} else if (!domains.isEmpty()) {
					browsingDomainCombo.setSelectedIndex(0);
				} else {
					browsingDomainCombo.setSelectedIndex(-1);
				}
			}
			
		}.execute();
	}

	private void browsingDomainChanged() {
		new SwingWorker<Void, Void>() {
		
			private final String domain = (String) browsingDomainCombo.getSelectedItem();
			
			private DefaultListModel model;
			private DnsSDBrowser browser;
		
			@Override
			protected Void doInBackground() throws Exception {
				model = new DefaultListModel();
				if (domain != null && !domain.isEmpty()) {
					browser = DnsSDFactory.getInstance().createBrowser(domain);
					Collection<ServiceType> types = browser.getServiceTypes();
					for (ServiceType serviceType : types) {
						model.addElement(serviceType);
					}
				}
				return null;
			}

			@Override
			protected void done() {
				serviceBrowser = browser;
				serviceTypeList.setModel(model);
			}

		}.execute();
	}

	private void serviceTypeChanged() {
		new SwingWorker<Void, Void>() {

			private final ServiceType type = (ServiceType) serviceTypeList.getSelectedValue();
			private final String subtypeList = subtypeField.getText();
			private final DnsSDBrowser browser = serviceBrowser;
			
			private DefaultListModel model;

			@Override
			protected Void doInBackground() throws Exception {
				model = new DefaultListModel();
				if (type != null && browser != null) {
					ServiceType typeToBrowse = type;
					if (!subtypeList.isEmpty()) {
						String[] subtypes = subtypeList.split(",");
						typeToBrowse = typeToBrowse.withSubtypes(subtypes);
					}
					Collection<ServiceName> instances = browser.getServiceInstances(typeToBrowse);
					for (ServiceName serviceName : instances) {
						model.addElement(serviceName);
					}
				}
				return null;
			}

			@Override
			protected void done() {
				serviceInstanceList.setModel(model);
			}

		}.execute();
	}

	private void serviceInstanceChanged() {
		new SwingWorker<Void, Void>() {

			private final ServiceName service = (ServiceName) serviceInstanceList.getSelectedValue();
			private final DnsSDBrowser browser = serviceBrowser;

			private String name;
			private String host;
			private String port;
			private DefaultTableModel model;

			@Override
			protected Void doInBackground() throws Exception {
				model = new DefaultTableModel(new Object[] { "Key", "Value" } , 0) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				if (service != null && browser != null) {
					ServiceData data = browser.getServiceData(service);
					name = data.getName().toString();
					host = data.getHost();
					port = Integer.toString(data.getPort());
					for (Map.Entry<String, String> entry : data.getProperties().entrySet()) {
						model.addRow(new Object[] { entry.getKey(), entry.getValue() });
					}
				}
				return null;
			}

			@Override
			protected void done() {
				nameField.setText(name);
				hostField.setText(host);
				portField.setText(port);
				propertiesTabel.setModel(model);
			}

		}.execute();
	}

	public class Listener implements ActionListener, ListSelectionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == computerDomainField) {
				computerDomainChanged();
			} else if (e.getSource() == browsingDomainCombo) {
				browsingDomainChanged();
			} else if (e.getSource() == subtypeField) {
				serviceTypeChanged();
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (e.getSource() == serviceTypeList) {
				serviceTypeChanged();
			} else if (e.getSource() == serviceInstanceList) {
				serviceInstanceChanged();
			}
		}

	}

}
