package org.esa.snap.product.library.ui.v2;

import org.esa.snap.ui.loading.LabelListCellRenderer;
import org.esa.snap.ui.loading.SwingUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Stack;

/**
 * Created by jcoravu on 20/8/2019.
 */
public class QueryParametersPanel extends JPanel {

    private JComboBox<AbstractProductsDataSource> dataSourcesComboBox;
    private final JButton searchButton;
    private final JButton helpButton;
    private JLabel dataSourceLabel;

    public QueryParametersPanel(Insets defaultListItemMargins, int textFieldPreferredHeight, int gapBetweenRows, int gapBetweenColumns,
                                ActionListener searchButtonListener, ItemListener dataSourceListener, IMissionParameterListener missionParameterListener) {

        super(new BorderLayout());

        createDataSourcesComboBox(textFieldPreferredHeight, defaultListItemMargins, gapBetweenRows, gapBetweenColumns, dataSourceListener, missionParameterListener);

        Dimension buttonSize = new Dimension(textFieldPreferredHeight, textFieldPreferredHeight);

        this.searchButton = buildButton("/org/esa/snap/productlibrary/icons/search24.png", searchButtonListener, buttonSize);

        ActionListener helpButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        this.helpButton = buildButton("/org/esa/snap/resources/images/icons/Help24.gif", helpButtonListener, buttonSize);

        this.dataSourceLabel = new JLabel("Data source");
        JPanel dataSourcesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = SwingUtils.buildConstraints(0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 0, 0);
        dataSourcesPanel.add(this.dataSourceLabel, c);
        c = SwingUtils.buildConstraints(1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        dataSourcesPanel.add(this.dataSourcesComboBox, c);
        c = SwingUtils.buildConstraints(2, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        dataSourcesPanel.add(this.searchButton, c);
        c = SwingUtils.buildConstraints(3, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        dataSourcesPanel.add(this.helpButton, c);

        add(dataSourcesPanel, BorderLayout.NORTH);

        addDataSourcePanel(getSelectedDataSource());
    }

    public AbstractProductsDataSource getSelectedDataSource() {
        return (AbstractProductsDataSource)this.dataSourcesComboBox.getSelectedItem();
    }

    public void refreshDataSourceMissionParameters() {
        getSelectedDataSource().refreshMissionParameters();
        refreshDataSourceLabelWidth();
    }

    public void refreshDataSourceParameters() {
        AbstractProductsDataSource selectedDataSource = getSelectedDataSource();
        int childrenCount = getComponentCount();
        for (int i=0; i<childrenCount; i++) {
            Component child = getComponent(i);
            if (child instanceof AbstractProductsDataSource) {
                remove(i);
                break;
            }
        }
        addDataSourcePanel(selectedDataSource);
        revalidate();
        repaint();
    }

    public void setParametersEnabledWhileDownloading(boolean enabled) {
        this.searchButton.setEnabled(enabled);
        AbstractProductsDataSource selectedDataSource = getSelectedDataSource();
        Stack<JComponent> stack = new Stack<JComponent>();
        stack.push(selectedDataSource);
        while (!stack.isEmpty()) {
            JComponent component = stack.pop();
            component.setEnabled(enabled);
            int childrenCount = component.getComponentCount();
            for (int i=0; i<childrenCount; i++) {
                Component child = component.getComponent(i);
                if (child instanceof JComponent) {
                    JComponent childComponent = (JComponent) child;
                    // add the component in the stack to be enabled/disabled
                    stack.push(childComponent);
                }
            }
        }
    }

    private void addDataSourcePanel(AbstractProductsDataSource selectedDataSource) {
        add(selectedDataSource, BorderLayout.CENTER);
        refreshDataSourceLabelWidth();
    }

    private void refreshDataSourceLabelWidth() {
        int maximumLabelWidth = getSelectedDataSource().computeLeftPanelMaximumLabelWidth();
        SciHubProductsDataSource.setLabelSize(this.dataSourceLabel, maximumLabelWidth);
        this.dataSourceLabel.getParent().revalidate();
        this.dataSourceLabel.getParent().repaint();
    }

    private void createDataSourcesComboBox(int textFieldPreferredHeight, Insets defaultListItemMargins, int gapBetweenRows, int gapBetweenColumns,
                                           ItemListener dataSourceListener, IMissionParameterListener missionParameterListener) {

        AbstractProductsDataSource[] availableDataSources = new AbstractProductsDataSource[2];
        availableDataSources[0] = new SciHubProductsDataSource(textFieldPreferredHeight, defaultListItemMargins, gapBetweenRows, gapBetweenColumns, missionParameterListener);
        availableDataSources[1] = new LocalProductsDataSource();

        this.dataSourcesComboBox = new JComboBox<AbstractProductsDataSource>(availableDataSources);
        Dimension comboBoxSize = this.dataSourcesComboBox.getPreferredSize();
        comboBoxSize.height = textFieldPreferredHeight;
//        this.dataSourcesComboBox.setPreferredSize(comboBoxSize);
        LabelListCellRenderer<AbstractProductsDataSource> renderer = new LabelListCellRenderer<AbstractProductsDataSource>(defaultListItemMargins) {
            @Override
            protected String getItemDisplayText(AbstractProductsDataSource value) {
                return (value == null) ? "" : value.getName();
            }
        };
        this.dataSourcesComboBox.setMaximumRowCount(5);
        this.dataSourcesComboBox.setRenderer(renderer);
        this.dataSourcesComboBox.setBackground(new Color(0, 0, 0, 0)); // set the transparent color
        this.dataSourcesComboBox.setOpaque(true);
        this.dataSourcesComboBox.setSelectedIndex(0);
        this.dataSourcesComboBox.addItemListener(dataSourceListener);
    }

    private static JButton buildButton(String resourceImagePath, ActionListener buttonListener, Dimension buttonSize) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL imageURL = classLoader.getResource(resourceImagePath);
        ImageIcon icon = new ImageIcon(imageURL);
        JButton button = new JButton(icon);
        button.setFocusable(false);
        button.addActionListener(buttonListener);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        return button;
    }
}
