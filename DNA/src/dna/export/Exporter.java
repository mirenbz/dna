package dna.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;

import dna.Dna;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

/**
 * @author Philip Leifeld
 * 
 * Contains everything needed to export networks, including GUI and network export algorithms.
 */
@SuppressWarnings("serial")
public class Exporter extends JDialog {
	JCheckBox helpBox;
	
	/**
	 * Opens an Exporter window, which displays the GUI for exporting network data.
	 */
	public Exporter() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new BorderLayout());
		
		JPanel settingsPanel = new JPanel();
		GridBagLayout g = new GridBagLayout();
		settingsPanel.setLayout(g);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		
		// first row of options
		JLabel networkModesLabel = new JLabel("Type of network");
		String networkModesToolTip = "<html><p width=\"500\">Select the type of network to export. A <strong>one-mode "
				+ "network</strong> has the same nodes in the rows and columns of the resulting matrix (e.g., "
				+ "organizations x organizations). A <strong>two-mode network</strong> has different sets of nodes in the "
				+ "rows and columns of the resulting matrix (e.g., organizations x concepts). An <strong>event list</strong> "
				+ "is a time-stamped list of edges, with each row of the resulting table containing all variables of a "
				+ "statement including the time.</p></html>";
		networkModesLabel.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesLabel, gbc);
		
		gbc.gridx = 1;
		JLabel statementTypeLabel = new JLabel("Statement type");
		String statementTypeToolTip = 
				"<html><p width=\"500\">A network is constructed based on the variables from a specific statement type. "
				+ "Select here which statement type to use.</p></html>";
		statementTypeLabel.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeLabel, gbc);
		
		gbc.gridx = 2;
		JLabel fileFormatLabel = new JLabel("File format");
		String fileFormatToolTip = "<html><p width=\"500\">Select the file format in which the resulting network is saved. "
				+ "<strong>CSV</strong> files can be read by most programs, including spreadsheet software like Microsoft "
				+ "Excel. <strong>DL</strong> files are plain text files which can be imported by the network analysis "
				+ "software Ucinet. <strong>GRAPHML</strong> files can be opened using the network visualization software "
				+ "visone. Note that the flavor of GRAPHML used here is specific to visone.</p></html>";
		fileFormatLabel.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		String[] networkModesItems = new String[] {"Two-mode network", "One-mode network", "Event list"};
		JComboBox<String> networkModesBox = new JComboBox<>(networkModesItems);
		networkModesBox.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesBox, gbc);

		gbc.gridx = 1;
		StatementTypeComboBoxRenderer cbrenderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		JComboBox<StatementType> statementTypeBox = new JComboBox<>(model);
		statementTypeBox.setRenderer(cbrenderer);
		statementTypeBox.setToolTipText(statementTypeToolTip);
		
		String[] var1Items = null, var2Items = null;
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			String[] vars = getVariablesList(Dna.data.getStatementTypes().get(i), false, true, false, false);
			if (vars.length > 1) {
				statementTypeBox.setSelectedItem(Dna.data.getStatementTypes().get(i));
				var1Items = vars;
				var2Items = vars;
				break;
			}
		}
		if (var1Items == null) {
			System.err.println("No statement type with more than one short text variable found!");
		}
		
		settingsPanel.add(statementTypeBox, gbc);
		int HEIGHT = (int) statementTypeBox.getPreferredSize().getHeight();
		int WIDTH = 200;
		Dimension d = new Dimension(WIDTH, HEIGHT);
		networkModesBox.setPreferredSize(d);
		
		gbc.gridx = 2;
		String[] fileFormatItems = new String[] {".csv", ".dl", ".graphml"};
		JComboBox<String> fileFormatBox = new JComboBox<>(fileFormatItems);
		fileFormatBox.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatBox, gbc);
		fileFormatBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		statementTypeBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// second row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		JLabel var1Label = new JLabel("Variable 1");

		String var1ToolTip = "<html><p width=\"500\">In a one-mode network, the first variable denotes the node class used "
				+ "both for the rows and columns of the matrix. For example, select the variable for organizations in order "
				+ "to export an organization x organization network. In a two-mode network, the first variable denotes the "
				+ "node class for the rows. For example, select the variable for organizations here if you want to export "
				+ "an organization x concept two-mode network.</p></html>";
		var1Label.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Label, gbc);
		
		gbc.gridx = 1;
		JLabel var2Label = new JLabel("Variable 2");
		String var2ToolTip = "<html><p width=\"500\">In a one-mode network, the second variable denotes the variable through "
				+ "which the edges are aggregated. For example, if you export a one-mode network of organizations and the "
				+ "edge weight that connects any two organizations should denote the two organizations' number of joint "
				+ "concepts, then the second variable should denote the concepts. In a two-mode network, the second variable "
				+ "denotes the node class used for the columns of the resulting network matrix. For example, one would select "
				+ "the variable for concepts here in order to export an organization x concept network.</p></html>";
		var2Label.setToolTipText(var2ToolTip);
		settingsPanel.add(var2Label, gbc);

		gbc.gridx = 2;
		JLabel qualifierLabel = new JLabel("Qualifier");
		String qualifierToolTip = "<html><p width=\"500\">The qualifier is a binary or integer variable which indicates "
				+ "different qualities or levels of association between variable 1 and variable 2. If a binary qualifier "
				+ "variable is used, this amounts to positive versus negative relations or agreement versus disagreement. "
				+ "For example, in an organization x concept two-mode network, a qualifier variable could indicate whether "
				+ "an organization supports or rejects a concept. The qualifier would indicate support or rejection in this "
				+ "case. The qualifier is also preserved in a one-mode network. For example, in an organization x organization "
				+ "network, any two organizations can be connected if they either co-support or co-reject the same concept. "
				+ "In other words, a one-mode projection of the support two-mode network and a one-mode projection of the "
				+ "rejection two-mode network are stacked and added in order to obtain a congruence network. If an integer "
				+ "(non-binary) qualifier variable is used, this indicates different intensities in a two-mode network, for "
				+ "example the extent of support (in the positive range) or rejection (in the negative range) of a concept by "
				+ "an organization. In a one-mode network, differences in these levels of intensity are used as edge weights; "
				+ "the description under Qualifier aggregation contains more details on this.</p></html>";
		qualifierLabel.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierLabel, gbc);

		gbc.gridx = 3;
		JLabel aggregationLabel = new JLabel("Qualifier aggregation");
		String aggregationToolTip = "<html><p width=\"500\">The choices differ between one-mode and two-mode networks. <strong>"
				+ "ignore</strong> is available in both cases and means that the agreement qualifier variable is ignored, i.e., "
				+ "the network is constructed as if all values on the qualifier variable were the same. In the two-mode network "
				+ "case, <strong>subtract</strong> means that negative values on the qualifier variable are subtracted from "
				+ "positive values (if an integer qualifier is selected) or that 0 values are subtracted from 1 values (if a "
				+ "binary qualifier variable is selected). For example, if an organization mentions a concept two times in a "
				+ "positive way and three times in a negative way, there will be an edge weight of -1 between the organization "
				+ "and the concept. In the one-mode case, <strong>subtract</strong> means that a congruence network and a "
				+ "conflict network are created separately and then the conflict network ties are subtracted from the congruence "
				+ "network ties. For example, if two organizations A and B disagree over 3 concepts and agree with regard to 5 "
				+ "concepts, they have an edge weight of 2. <strong>combine</strong> is only available for two-mode networks. "
				+ "In contrast to the subtract option, it means that the values of the qualifier are treated as qualitative "
				+ "categories. In this case, DNA creates all possible combinations of edges (e.g., in the binary case, these are "
				+ "support, rejection, and both/mixed/ambiguous). Integer values are then used as edge weights, as in a multiplex "
				+ "network. E.g., 1 represents support, 2 represents rejection, and 3 represents both. With an integer variable, "
				+ "this may become more complex. In one-mode networks, <strong>congruence</strong> means that similarity or matches "
				+ "on the qualifier variable are counted in order to construct an edge. With a binary variable, matches are used. "
				+ "For example, if organizations A and B both co-support or both co-reject concept C, they are connected, and the "
				+ "number of co-supported and co-rejected concepts is used as the edge weight. With an integer qualifier variable, "
				+ "the inverse of the absolute distance plus one (i.e., the proximity) is used instead of a match. In one-mode "
				+ "networks, <strong>conflict</strong> constructs the network by recording disagreements between actors, rather "
				+ "than agreements, as in the congruence case. With a binary qualifier, this means that organizations A and B "
				+ "are connected if one of them supports a concept and the other one rejects the concept. With an integer qualifier, "
				+ "the absolute distance is used instead.</p></html>";
		aggregationLabel.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 3;
		JComboBox<String> var1Box = new JComboBox<>(var1Items);
		var1Box.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		JComboBox<String> var2Box = new JComboBox<>(var2Items);
		var2Box.setToolTipText(var2ToolTip);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		//String[] qualifierItems = getVariablesList((StatementType) statementTypeBox.getSelectedItem(), false, false, true, true);
		String[] qualifierItems = new String[0];
		JComboBox<String> qualifierBox = new JComboBox<>(qualifierItems);
		qualifierBox.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierBox, gbc);
		qualifierBox.setEnabled(false);
		qualifierBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 3;
		String[] aggregationItems = new String[] {"ignore", "combine", "subtract"};
		JComboBox<String> aggregationBox = new JComboBox<>(aggregationItems);
		aggregationBox.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationBox, gbc);
		aggregationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// third row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 4;
		JLabel normalizationLabel = new JLabel("Normalization");
		String normalizationToolTip = "<html><p width=\"500\">Normalization of edge weights serves to cancel out the effect "
				+ "that some nodes are more active than others. For example, in an organization x organization one-mode "
				+ "network aggregated through a concept variable, some organizations with an institutional mandate end "
				+ "up in very central locations of the network because they have agreement with many other actors due to "
				+ "their role rather than their actual opinion, i.e., because their office requires them to reveal their "
				+ "preferences on many concepts. To cancel out this activity effect, one can divide the edge weight "
				+ "between two organizations by a function of the two incident organizations' activity or frequency of "
				+ "making statements. This normalization procedure leaves us with a network of edge weights that reflect "
				+ "similarity in opinions without taking into account centrality or activity in any way. There are several "
				+ "ways to normalize a one-mode network. <strong>No normalization</strong> switches off normalization and "
				+ "is the default value. <strong>Average activity</strong> divides edge weights between first-variable "
				+ "nodes by the average number of different second-variable nodes they are adjacent with in a two-mode "
				+ "network. For example, if organization A makes statements about 20 different concepts and B makes "
				+ "statements about 60 difference concepts, the edge weight between A and B in the congruence network "
				+ "is divided by 40. To achieve a better scaling, all edge weights in the resulting normalized one-mode "
				+ "network matrix are scaled between 0 and 1. <strong>Jaccard similarity</strong> is a similarity measure "
				+ "with known normalizing properties as well. Rather than average activity, it divides the co-occurrence "
				+ "frequency by the activity count of both separate actors plus their joint activity. <strong>Cosine "
				+ "similarity </strong>is another similarity measure with normalizing properties. It divides edge weights "
				+ "by the product of the nodes' activity. Details on these normalization measures can be found in <emph>"
				+ "Leifeld, Philip (2017): Discourse Network Analysis: Policy Debates as Dynamic Networks. In: Oxford "
				+ "Handbook of Political Networks, edited by JN Victor, AH Montgomery, and MN Lubell, chapter 25, Oxford "
				+ "University Press.</emph> With two-mode networks, <strong>activity</strong> normalization and <strong>"
				+ "prominence</strong> normalization are available. They divide the edge weights through the activity of "
				+ "the node from the first or the second mode, respectively.</p></html>";
		normalizationLabel.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationLabel, gbc);
		
		gbc.gridx = 1;
		JLabel isolatesLabel = new JLabel("Isolates");
		String isolatesToolTip = "<html><p width=\"500\">Often, one would like to export several time slices or networks "
				+ "for different qualifier values separately etc. and merge the resulting networks later on manually. "
				+ "In these situations, it is easier to merge multiple networks if they have the same matrix dimensions. "
				+ "However, this is usually not the case because the network export functions by default only include "
				+ "nodes that show at least minimal activity in the network that is exported. To achieve compatibility of "
				+ "the matrix dimensions anyway, it is possible to include all nodes of the selected variable(s) in the "
				+ "whole database, irrespective of time, qualifiers, and excluded values (but without any edge weights "
				+ "larger than 0, i.e., as isolates). This is meant by including isolates.</p></html>";
		isolatesLabel.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesLabel, gbc);

		gbc.gridx = 2;
		JLabel duplicatesLabel = new JLabel("Duplicates");
		String duplicatesToolTip = "<html><p width=\"500\">By default, all statements are included in a network export. "
				+ "However, in many data sources, repeated identical statements within the same document are an "
				+ "artifact of the source material. For example, in a newspaper article, the number of times an actor "
				+ "is quoted with a statement may be a function of the journalist's agenda or the reporting style of "
				+ "the news media outlet, rather than the actor's deliberate attempt to speak multiple times. In cases "
				+ "like this it makes sense to count each statement only once per document. Alternatively, it may "
				+ "make sense to count statements only once per week, month, or year or in the whole time period if this "
				+ "artifact happens across documents (e.g., if a newspaper reprints interviews or reports on the same "
				+ "press release multiple times in different documents).</p></html>";
		duplicatesLabel.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		String[] normalizationItems = new String[0];
		JComboBox<String> normalizationBox = new JComboBox<>(normalizationItems);
		normalizationBox.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setEnabled(false);
		normalizationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		String[] isolatesItems = new String[] {"include isolates", "only current nodes"};
		JComboBox<String> isolatesBox = new JComboBox<>(isolatesItems);
		isolatesBox.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesBox, gbc);
		isolatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document", "ignore per calendar week", 
				"ignore per calendar month", "ignore per calendar year", "ignore across date range"};
		JComboBox<String> duplicatesBox = new JComboBox<>(duplicatesItems);
		duplicatesBox.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// fourth row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 6;
		JLabel startLabel = new JLabel("Include from");
		String dateToolTip = "<html><p width=\"500\">All statements before the start date and time and after the stop date "
				+ "and time will be ignored during network export. This can be helpful for exporting multiple time slices "
				+ "and analyzing them separately. By default, the first and last date/time stamp in any statement found in "
				+ "the whole database are used, i.e., the default setting includes all statements.</p></html>";
		startLabel.setToolTipText(dateToolTip);
		settingsPanel.add(startLabel, gbc);
		
		gbc.gridx = 1;
		JLabel stopLabel = new JLabel("Include until");
		stopLabel.setToolTipText(dateToolTip);
		settingsPanel.add(stopLabel, gbc);

		gbc.gridx = 2;
		JLabel temporalLabel = new JLabel("Temporal aggregation");
		String temporalToolTip = "<html><p width=\"500\">By default, one-mode networks are creating by aggregating over "
				+ "the qualifier variable across the whole date range. In some settings, however, it makes sense to "
				+ "create edges only within documents (but still taking into account all documents at all time points). "
				+ "For example, if the context of the second variable differs across documents, such as norms in legal "
				+ "cases, it makes sense to create edges between nodes within the first case, then only within the "
				+ "second case, then the third etc., and finally merge the resulting networks into a joint network. "
				+ "This is what is done when edges are nested within documents.</p></html>";
		temporalLabel.setToolTipText(temporalToolTip);
		settingsPanel.add(temporalLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 7;
		SpinnerDateModel startModel = new SpinnerDateModel();
		JSpinner startSpinner = new JSpinner();
		startModel.setCalendarField(Calendar.DAY_OF_YEAR);
		startSpinner.setModel(startModel);
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			dates.add(Dna.data.getStatements().get(i).getDate());
		}
		Collections.sort(dates);
		startModel.setValue(dates.get(0));
		startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd - HH:mm:ss"));
		startSpinner.setToolTipText(dateToolTip);
		settingsPanel.add(startSpinner, gbc);
		startSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		SpinnerDateModel stopModel = new SpinnerDateModel();
		JSpinner stopSpinner = new JSpinner();
		stopModel.setCalendarField(Calendar.DAY_OF_YEAR);
		stopSpinner.setModel(stopModel);
		stopModel.setValue(dates.get(dates.size() - 1));
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "yyyy-MM-dd - HH:mm:ss"));
		stopSpinner.setToolTipText(dateToolTip);
		settingsPanel.add(stopSpinner, gbc);
		stopSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 2;
		String[] temporalItems = new String[] {"across date range", "nested by document"};
		JComboBox<String> temporalBox = new JComboBox<>(temporalItems);
		temporalBox.setToolTipText(temporalToolTip);
		settingsPanel.add(temporalBox, gbc);
		temporalBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// fifth row of options: exclude values from variables
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 8;
		JLabel excludeVariableLabel = new JLabel("Exclude from variable");
		String excludeToolTip = "<html><p width=\"500\">By default, all nodes from all statements are included in the "
				+ "network, given the type of network and the statement type and variables selected. Often, however, "
				+ "one wishes to exclude certain nodes or statements from the analysis. For example, in an "
				+ "organization x organization congruence network via concepts, if one already knows that a certain "
				+ "concept is very general and does not add to the structure of the discourse network, one can "
				+ "exclude this concept. Or if the analysis should be restricted to a subgroup of actors (e.g., only "
				+ "legislators), one can select multiple actors that do not belong to this subgroup, and they will be "
				+ "ignored during network export. The way this process works is that the list of statements from which "
				+ "the network is generated is first filtered, and only statements that do not contain any of the "
				+ "values to be excluded will be retained for the export. To do this, one first needs to select a "
				+ "variable on the left and then select the values to be excluded. To select or unselect multiple "
				+ "values, one can hold the ctrl key while selecting or unselecting additional values. The preview "
				+ "on the right lists all variable-value combinations that are excluded.</p></html>";
		excludeVariableLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableLabel, gbc);
		
		gbc.gridx = 1;
		JLabel excludeValuesLabel = new JLabel("Exclude values");
		excludeValuesLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValuesLabel, gbc);
		
		gbc.gridx = 2;
		JLabel excludePreviewLabel = new JLabel("Preview of excluded values");
		excludePreviewLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewLabel, gbc);
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 9;
		JList<String> excludeVariableList = new JList<String>();
		excludeVariableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		excludeVariableList.setLayoutOrientation(JList.VERTICAL);
		excludeVariableList.setVisibleRowCount(10);
		JScrollPane excludeVariableScroller = new JScrollPane(excludeVariableList);
		excludeVariableScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeVariableList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableScroller, gbc);
		excludeVariableScroller.setPreferredSize(new Dimension(WIDTH, (int) excludeVariableScroller.getPreferredSize().getHeight()));
		
		gbc.gridx = 1;
		JList<String> excludeValueList = new JList<String>();
		excludeValueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeValueList.setLayoutOrientation(JList.VERTICAL);
		excludeValueList.setVisibleRowCount(10);
		JScrollPane excludeValueScroller = new JScrollPane(excludeValueList);
		excludeValueScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeValueList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValueScroller, gbc);
		excludeValueScroller.setPreferredSize(new Dimension(WIDTH, (int) excludeValueScroller.getPreferredSize().getHeight()));
		
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		JTextArea excludePreviewArea = new JTextArea();
		excludePreviewArea.setPreferredSize(excludeValueScroller.getPreferredSize());
		excludePreviewArea.setText("This is a test.");
		excludePreviewArea.setEditable(false);
		excludePreviewArea.setBackground(settingsPanel.getBackground());
		JScrollPane excludePreviewScroller = new JScrollPane(excludePreviewArea);
		excludePreviewArea.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewScroller, gbc);
		
		// sixth row: buttons
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 10;
		helpBox = new JCheckBox("Display tooltips with instructions");
		helpBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleHelp();
			}
			
		});
		settingsPanel.add(helpBox, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton revertButton = new JButton("Revert", new ImageIcon(getClass().getResource("/icons/arrow_rotate_anticlockwise.png")));
		String revertToolTip = "<html><p>Reset all settings to their default values.</p></html>";
		revertButton.setToolTipText(revertToolTip);
		buttonPanel.add(revertButton);
		JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		String cancelToolTip = "<html><p>Reset and close this window.</p></html>";
		cancelButton.setToolTipText(cancelToolTip);
		buttonPanel.add(cancelButton);
		JButton exportButton = new JButton("Export...", new ImageIcon(getClass().getResource("/icons/accept.png")));
		String exportToolTip = "<html><p>Select a file name and save the network using the current settings.</p></html>";
		exportButton.setToolTipText(exportToolTip);
		buttonPanel.add(exportButton);
		toggleHelp();
		settingsPanel.add(buttonPanel, gbc);
		
		this.add(settingsPanel, BorderLayout.NORTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void toggleHelp() {
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(10);
		if (helpBox.isSelected()) {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(true);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(30000);
		} else {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(0);
		}
	}
	
	/**
	 * Returns a String array with the variables of the {@link StatementType} selected to fill a {@link JComboBox}.
	 * 
	 * @param longtext	boolean indicating whether long text variables should be included.
	 * @param shorttext	boolean indicating whether short text variables should be included.
	 * @param integer	boolean indicating whether integer variables should be included.
	 * @param bool		boolean indicating whether boolean variables should be included.
	 * @return			{@link String[]} with variables of the statementType selected.
	 */
	String[] getVariablesList(StatementType statementType, boolean longtext, boolean shorttext, boolean integer, boolean bool) {
		LinkedHashMap<String, String> variables = statementType.getVariables();
		Iterator<String> it = variables.keySet().iterator();
		ArrayList<String> items = new ArrayList<String>();
		while (it.hasNext()) {
			String var = it.next();
			if ((longtext == true && variables.get(var).equals("long text")) || 
					(shorttext == true && variables.get(var).equals("short text")) ||
					(integer == true && variables.get(var).equals("integer")) ||
					(bool == true && variables.get(var).equals("boolean"))) {
				items.add(var);
			}
		}
		if (shorttext == true) {
			items.add("author");
			items.add("source");
			items.add("section");
			items.add("type");
		}
		String[] vec = new String[items.size()];
		if (vec.length > 0) {
			for (int i = 0; i < items.size(); i++) {
				vec[i] = items.get(i);
			}
		}
		return vec;
	}
}
