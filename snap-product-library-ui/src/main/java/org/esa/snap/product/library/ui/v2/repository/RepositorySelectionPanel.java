package org.esa.snap.product.library.ui.v2.repository;

import org.esa.snap.product.library.ui.v2.ComponentDimension;
import org.esa.snap.product.library.ui.v2.IMissionParameterListener;
import org.esa.snap.product.library.ui.v2.thread.ProgressPanel;
import org.esa.snap.product.library.v2.repository.ProductsRepositoryProvider;
import org.esa.snap.ui.loading.LabelListCellRenderer;
import org.esa.snap.ui.loading.SwingUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Stack;

/**
 * Created by jcoravu on 22/8/2019.
 */
public class RepositorySelectionPanel extends JPanel implements ProgressPanel {

    private JComboBox<AbstractProductsRepositoryPanel> repositoriesComboBox;
    private final JButton searchButton;
    private final JButton helpButton;
    private final JLabel repositoryLabel;
    private final JButton stopButton;
    private final JProgressBar progressBar;

    private int currentThreadId;

    public RepositorySelectionPanel(ProductsRepositoryProvider[] dataSourceProductProviders, ComponentDimension componentDimension, ActionListener searchButtonListener,
                                    ItemListener dataSourceListener, ActionListener stopButtonListener, IMissionParameterListener missionParameterListener) {

        super(new GridBagLayout());

        this.currentThreadId = 0;

        createRepositoriesComboBox(dataSourceProductProviders, componentDimension, dataSourceListener, missionParameterListener);

        Dimension buttonSize = new Dimension(componentDimension.getTextFieldPreferredHeight(), componentDimension.getTextFieldPreferredHeight());

        this.searchButton = buildButton("/org/esa/snap/productlibrary/icons/search24.png", searchButtonListener, buttonSize);
        this.searchButton.setToolTipText("Search");

        ActionListener helpButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        this.helpButton = buildButton("/org/esa/snap/resources/images/icons/Help24.gif", helpButtonListener, buttonSize);
        this.helpButton.setToolTipText("Help");

        this.stopButton = buildButton("/org/esa/snap/productlibrary/icons/stop20.gif", stopButtonListener, buttonSize);
        this.stopButton.setToolTipText("Stop");

        this.repositoryLabel = new JLabel("Repository");

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(100, 10));
        this.progressBar.setMinimumSize(new Dimension(100, 10));

        refreshRepositoryLabelWidth();
        setProgressPanelVisible(false);

        int gapBetweenColumns = componentDimension.getGapBetweenColumns();

        GridBagConstraints c = SwingUtils.buildConstraints(0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 0, 0);
        add(this.repositoryLabel, c);
        c = SwingUtils.buildConstraints(1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.repositoriesComboBox, c);
        c = SwingUtils.buildConstraints(2, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.searchButton, c);
        c = SwingUtils.buildConstraints(3, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.helpButton, c);
        c = SwingUtils.buildConstraints(4, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(progressBar, c);
        c = SwingUtils.buildConstraints(5, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.stopButton, c);
    }

    @Override
    public boolean isCurrentThread(int threadId) {
        if (EventQueue.isDispatchThread()) {
            return (this.currentThreadId == threadId);
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    @Override
    public boolean hideProgressPanel(int threadId) {
        if (EventQueue.isDispatchThread()) {
            if (this.currentThreadId == threadId) {
                this.currentThreadId++;
                setProgressPanelVisible(false);
                setParametersEnabledWhileDownloading(true);
                return true;
            }
            return false;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    @Override
    public boolean showProgressPanel(int threadId) {
        if (EventQueue.isDispatchThread()) {
            if (this.currentThreadId == threadId) {
                setProgressPanelVisible(true);
                setParametersEnabledWhileDownloading(false);
                return true;
            }
            return false;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public final int incrementAndGetCurrentThreadId() {
        if (EventQueue.isDispatchThread()) {
            return ++this.currentThreadId;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public void hideProgressPanel() {
        if (EventQueue.isDispatchThread()) {
            this.currentThreadId++;
            setProgressPanelVisible(false);
            setParametersEnabledWhileDownloading(true);
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public AbstractProductsRepositoryPanel getSelectedDataSource() {
        return (AbstractProductsRepositoryPanel)this.repositoriesComboBox.getSelectedItem();
    }

    public void refreshDataSourceMissionParameters() {
        getSelectedDataSource().refreshMissionParameters();
        refreshRepositoryLabelWidth();
    }

    public void setDataSourcesBorder(Border border) {
        int count = this.repositoriesComboBox.getModel().getSize();
        for (int i=0; i<count; i++) {
            AbstractProductsRepositoryPanel productsDataSource = this.repositoriesComboBox.getModel().getElementAt(i);
            productsDataSource.setBorder(border);
        }
    }

    public void addNewLocalFolderProductsDataSource(LocalFolderProductsRepositoryPanel localFolderProductsDataSourcePanel) {
        this.repositoriesComboBox.addItem(localFolderProductsDataSourcePanel);
    }

    private void setParametersEnabledWhileDownloading(boolean enabled) {
        this.searchButton.setEnabled(enabled);
        this.repositoryLabel.setEnabled(enabled);
        this.repositoriesComboBox.setEnabled(enabled);
        AbstractProductsRepositoryPanel selectedDataSource = getSelectedDataSource();
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

    private void setProgressPanelVisible(boolean visible) {
        this.progressBar.setVisible(visible);
        this.stopButton.setVisible(visible);
    }

    private void refreshRepositoryLabelWidth() {
        int maximumLabelWidth = getSelectedDataSource().computeLeftPanelMaximumLabelWidth();
        RemoteProductsRepositoryPanel.setLabelSize(this.repositoryLabel, maximumLabelWidth);
        Container parentContainer = this.repositoryLabel.getParent();
        if (parentContainer != null) {
            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    private void createRepositoriesComboBox(ProductsRepositoryProvider[] dataSourceProductProviders, ComponentDimension componentDimension,
                                            ItemListener dataSourceListener, IMissionParameterListener missionParameterListener) {

        AbstractProductsRepositoryPanel[] availableDataSources = new AbstractProductsRepositoryPanel[dataSourceProductProviders.length + 1];
        for (int i=0; i<dataSourceProductProviders.length; i++) {
            availableDataSources[i] = new RemoteProductsRepositoryPanel(dataSourceProductProviders[i], componentDimension, missionParameterListener);
        }
        availableDataSources[dataSourceProductProviders.length] = new AllLocalFolderProductsRepositoryPanel();

        this.repositoriesComboBox = new JComboBox<AbstractProductsRepositoryPanel>(availableDataSources) {
            @Override
            public Color getBackground() {
                return Color.WHITE;
            }
        };
        Dimension comboBoxSize = this.repositoriesComboBox.getPreferredSize();
        comboBoxSize.height = componentDimension.getTextFieldPreferredHeight();
        this.repositoriesComboBox.setPreferredSize(comboBoxSize);
        LabelListCellRenderer<AbstractProductsRepositoryPanel> renderer = new LabelListCellRenderer<AbstractProductsRepositoryPanel>(componentDimension.getListItemMargins()) {
            @Override
            protected String getItemDisplayText(AbstractProductsRepositoryPanel value) {
                return (value == null) ? "" : value.getName();
            }
        };
        this.repositoriesComboBox.setRenderer(renderer);
        this.repositoriesComboBox.setMaximumRowCount(5);
        this.repositoriesComboBox.setSelectedIndex(0);
        this.repositoriesComboBox.addItemListener(dataSourceListener);
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