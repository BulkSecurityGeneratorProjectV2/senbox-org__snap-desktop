package org.esa.snap.graphbuilder.gpf.ui;


import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.TableLayout;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.ImageGeometry;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductFilter;
import org.esa.snap.core.gpf.ui.CollocationCrsForm;
import org.esa.snap.core.util.Debug;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.AbstractDialog;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.DemSelector;
import org.esa.snap.ui.ModalDialog;
import org.esa.snap.ui.crs.CrsForm;
import org.esa.snap.ui.crs.CrsSelectionPanel;
import org.esa.snap.ui.crs.CustomCrsForm;
import org.esa.snap.ui.crs.OutputGeometryForm;
import org.esa.snap.ui.crs.OutputGeometryFormModel;
import org.esa.snap.ui.crs.PredefinedCrsForm;
import org.esa.snap.ui.product.ProductExpressionPane;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * User interface for Reprojection
 */
public class ReprojectionUI extends BaseOperatorUI {

    private static final String[] RESAMPLING_IDENTIFIER = {"Nearest", "Bilinear", "Bicubic"};

    private JScrollPane scrollPane;

    private boolean orthoMode = false;
    private AppContext appContext = SnapApp.getDefault().getAppContext();

    private DemSelector demSelector;
    private CrsSelectionPanel crsSelectionPanel;

    private OutputGeometryFormModel outputGeometryModel;

    private JButton outputParamButton;
    private ReprojectionUI.InfoForm infoForm;
    private CoordinateReferenceSystem crs;

    private JPanel maskExpressionPanel;

    //TODO add collocationCRSForm
    //private CollocationCrsForm collocationCrsUI;

    private CustomCrsForm customCrsUI;

    //Components of output setting panel
    final JCheckBox preserveResolutionCheckBox = new JCheckBox("Preserve resolution",true);
    JCheckBox includeTPcheck = new JCheckBox("Reproject tie-point grids", true);
    final JTextField noDataField = new JTextField(Double.toString(Double.NaN));
    JCheckBox addDeltaBandsChecker = new JCheckBox("Add delta lat/lon bands");
    JComboBox<String> resampleComboBox = new JComboBox<>(RESAMPLING_IDENTIFIER);


    // Components of Masking
    private boolean applyValidPixelExpression;
    private JCheckBox applyValidPixelExpressionCheckBox;
    private boolean transferValidPixelExpression;
    private JCheckBox transferValidPixelExpressionCheckBox;
    private JButton editExpressionButton;
    private JTextArea expressionArea;

    //Create panel
    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {


        initializeOperatorUI(operatorName, parameterMap);
        final JComponent panel = createPanel();

        initParameters();

        scrollPane = new JScrollPane(panel);
        return scrollPane;
    }

    //called when sourceProduct is set
    @Override
    public void initParameters() {
        if(hasSourceProducts() && sourceProducts[0] != null) {
            crsSelectionPanel.setReferenceProduct(sourceProducts[0]);
            if((sourceProducts[0].getBand("longitude") != null && sourceProducts[0].getBand("latitude") != null) || (sourceProducts[0].getTiePointGrid("longitude") != null && sourceProducts[0].getTiePointGrid("latitude") != null)) {
                addDeltaBandsChecker.setEnabled(true);
            } else {
                addDeltaBandsChecker.setEnabled(false);
            }
        }
        updateCRS();
        updateParameters();

    }

    @Override
    public UIValidation validateParameters() {
        return new UIValidation(UIValidation.State.OK, "");
    }

    @Override
    public void updateParameters() {


        paramMap.clear();
        paramMap.put("resamplingName", resampleComboBox.getSelectedItem().toString());
        paramMap.put("includeTiePointGrids", includeTPcheck.isSelected());
        paramMap.put("addDeltaBands", addDeltaBandsChecker.isSelected());
        paramMap.put("noDataValue", Double.parseDouble(noDataField.getText()));
        // if (!collocationCrsUI.getRadioButton().isSelected()) {
        CoordinateReferenceSystem selectedCrs = getSelectedCrs();
        if (selectedCrs != null) {
            paramMap.put("crs", selectedCrs.toWKT());
        } else {
            paramMap.put("crs", "EPSG:4326");
        }
        //   collocationCrsUI.prepareHide();
        // } else {
        //     //TODO
        //     final Map<String, Product> productMap = new HashMap<>(5);
        //     productMap.put("source", getSourceProduct());
        //     if (collocationCrsUI.getRadioButton().isSelected()) {
        //         collocationCrsUI.prepareShow();
        //         productMap.put("collocateWith", collocationCrsUI.getCollocationProduct());
        //     }
        // }


        if (orthoMode) {
            paramMap.put("orthorectify", orthoMode);
            if (demSelector.isUsingExternalDem()) {
                paramMap.put("elevationModelName", demSelector.getDemName());
            } else {
                paramMap.put("elevationModelName", null);
            }
        }

        if (!preserveResolutionCheckBox.isSelected() && outputGeometryModel != null) {
            PropertySet container = outputGeometryModel.getPropertySet();
            paramMap.put("referencePixelX", container.getValue("referencePixelX"));
            paramMap.put("referencePixelY", container.getValue("referencePixelY"));
            paramMap.put("easting", container.getValue("easting"));
            paramMap.put("northing", container.getValue("northing"));
            paramMap.put("orientation", container.getValue("orientation"));
            paramMap.put("pixelSizeX", container.getValue("pixelSizeX"));
            paramMap.put("pixelSizeY", container.getValue("pixelSizeY"));
            paramMap.put("width", container.getValue("width"));
            paramMap.put("height", container.getValue("height"));
        }


        applyValidPixelExpression = applyValidPixelExpressionCheckBox.isSelected();
        paramMap.put("applyValidPixelExpression", applyValidPixelExpression);

        transferValidPixelExpression = transferValidPixelExpressionCheckBox.isSelected();
        paramMap.put("transferValidPixelExpression", transferValidPixelExpression);

        if (expressionArea.getText() != null) {
            paramMap.put("maskExpression", expressionArea.getText());
        }



    }


    private JComponent createPanel() {
        final JPanel parameterPanel = new JPanel();
        final TableLayout layout = new TableLayout(1);
        layout.setTablePadding(4, 4);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableWeightX(1.0);
        parameterPanel.setLayout(layout);

        //Create panel with CrsForms
        customCrsUI = new CustomCrsForm(appContext);
        CrsForm predefinedCrsUI = new PredefinedCrsForm(appContext);
        //collocationCrsUI = new CollocationCrsForm(appContext);
        CrsForm[] crsForms = new CrsForm[]{customCrsUI, predefinedCrsUI/*, collocationCrsUI*/};
        crsSelectionPanel = new CrsSelectionPanel(crsForms);
        crsSelectionPanel.prepareShow();

        //add CrsPanel to parameter panel
        parameterPanel.add(crsSelectionPanel);

        //if orthoMode, create and add demSelector
        if (orthoMode) {
            demSelector = new DemSelector();
            parameterPanel.add(demSelector);
        }

        parameterPanel.add(createMaskSettingsPanel());


        //create and add the output setting panel
        parameterPanel.add(createOuputSettingsPanel());


        //create and add the info panel
        infoForm = new ReprojectionUI.InfoForm();
        parameterPanel.add(infoForm.createUI());



        //add change listener
        crsSelectionPanel.addPropertyChangeListener("crs", evt -> updateCRS());
        updateCRS();
        return parameterPanel;
    }



    Product getSourceProduct() {
        if(!hasSourceProducts()) {
            return null;
        }
        return sourceProducts[0];
    }

    CoordinateReferenceSystem getSelectedCrs() {
        return crs;
    }

    private void updateCRS() {
        final Product sourceProduct = getSourceProduct();
        try {
            if (sourceProduct != null) {
                crs = crsSelectionPanel.getCrs(ProductUtils.getCenterGeoPos(sourceProduct));
                infoForm.setCenterPos(ProductUtils.getCenterGeoPos(sourceProduct));
                if (outputGeometryModel != null) {
                    outputGeometryModel.setSourceProduct(sourceProduct);
                }
                if (crs != null) {
                    infoForm.setCrsInfoText(crs.getName().getCode(), crs.toString());
                } else {
                    infoForm.setCrsErrorText("No valid 'Coordinate Reference System' selected.");
                }
            } else {
                infoForm.setCrsErrorText("No source product selected.");
                crs = null;
            }
        } catch (FactoryException e) {
            infoForm.setCrsErrorText(e.getMessage());
            crs = null;
        }
        if (outputGeometryModel != null) {
            outputGeometryModel.setTargetCrs(crs);
        }
        updateOutputParameterState();
    }

    private void updateProductSize() {
        int width = 0;
        int height = 0;
        final Product sourceProduct = getSourceProduct();
        if (sourceProduct != null && crs != null) {
            if (!preserveResolutionCheckBox.isSelected() && outputGeometryModel != null) {
                PropertySet container = outputGeometryModel.getPropertySet();
                width = container.getValue("width");
                height = container.getValue("height");
            } else {
                ImageGeometry iGeometry;
                // final Product collocationProduct = collocationCrsUI.getCollocationProduct();
                // if (collocationCrsUI.getRadioButton().isSelected() && collocationProduct != null) {
                //     iGeometry = ImageGeometry.createCollocationTargetGeometry(sourceProduct, collocationProduct);
                // } else {
                iGeometry = ImageGeometry.createTargetGeometry(sourceProduct, crs,
                        null, null, null, null,
                        null, null, null, null,
                        null);

                // }
                Rectangle imageRect = iGeometry.getImageRect();
                width = imageRect.width;
                height = imageRect.height;
            }
        }
        infoForm.setWidth(width);
        infoForm.setHeight(height);
    }

    private class InfoForm {

        private JLabel widthLabel;
        private JLabel heightLabel;
        private JLabel centerLatLabel;
        private JLabel centerLonLabel;
        private JLabel crsLabel;
        private String wkt;
        private JButton wktButton;

        void setWidth(int width) {
            widthLabel.setText(Integer.toString(width));
        }

        void setHeight(int height) {
            heightLabel.setText(Integer.toString(height));
        }

        void setCenterPos(GeoPos geoPos) {
            if (geoPos != null) {
                centerLatLabel.setText(geoPos.getLatString());
                centerLonLabel.setText(geoPos.getLonString());
            } else {
                centerLatLabel.setText("");
                centerLonLabel.setText("");
            }
        }

        void setCrsErrorText(String infoText) {
            setCrsInfoText("<html><b>" + infoText + "</b>", null);
        }

        void setCrsInfoText(String infoText, String wkt) {
            this.wkt = wkt;
            crsLabel.setText(infoText);
            boolean hasWKT = (wkt != null);
            wktButton.setEnabled(hasWKT);
        }

        JPanel createUI() {
            widthLabel = new JLabel();
            heightLabel = new JLabel();
            centerLatLabel = new JLabel();
            centerLonLabel = new JLabel();
            crsLabel = new JLabel();

            final TableLayout tableLayout = new TableLayout(5);
            tableLayout.setTableAnchor(TableLayout.Anchor.WEST);
            tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
            tableLayout.setTablePadding(4, 4);
            tableLayout.setColumnWeightX(0, 0.0);
            tableLayout.setColumnWeightX(1, 0.0);
            tableLayout.setColumnWeightX(2, 1.0);
            tableLayout.setColumnWeightX(3, 0.0);
            tableLayout.setColumnWeightX(4, 1.0);
            tableLayout.setCellColspan(2, 1, 3);
            tableLayout.setCellPadding(0, 3, new Insets(4, 24, 4, 20));
            tableLayout.setCellPadding(1, 3, new Insets(4, 24, 4, 20));


            final JPanel panel = new JPanel(tableLayout);
            panel.setBorder(BorderFactory.createTitledBorder("Output Information"));
            panel.add(new JLabel("Scene width:"));
            panel.add(widthLabel);
            panel.add(new JLabel("pixel"));
            panel.add(new JLabel("Center longitude:"));
            panel.add(centerLonLabel);

            panel.add(new JLabel("Scene height:"));
            panel.add(heightLabel);
            panel.add(new JLabel("pixel"));
            panel.add(new JLabel("Center latitude:"));
            panel.add(centerLatLabel);

            panel.add(new JLabel("CRS:"));
            panel.add(crsLabel);
            wktButton = new JButton("Show WKT");
            wktButton.addActionListener(e -> {
                JTextArea wktArea = new JTextArea(30, 40);
                wktArea.setEditable(false);
                wktArea.setText(wkt);
                final JScrollPane scrollPane = new JScrollPane(wktArea);
                final ModalDialog dialog = new ModalDialog(appContext.getApplicationWindow(),
                        "Coordinate reference system as well known text",
                        scrollPane,
                        ModalDialog.ID_OK, null);
                dialog.show();
            });
            wktButton.setEnabled(false);
            panel.add(wktButton);
            return panel;
        }
    }

    private JPanel createOuputSettingsPanel() {
        final TableLayout tableLayout = new TableLayout(3);
        tableLayout.setTableAnchor(TableLayout.Anchor.WEST);
        tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        tableLayout.setColumnFill(0, TableLayout.Fill.NONE);
        tableLayout.setTablePadding(4, 4);
        tableLayout.setColumnPadding(0, new Insets(4, 4, 4, 20));
        tableLayout.setColumnWeightX(0, 0.0);
        tableLayout.setColumnWeightX(1, 0.0);
        tableLayout.setColumnWeightX(2, 1.0);
        tableLayout.setCellColspan(0, 1, 2);
        tableLayout.setCellPadding(1, 0, new Insets(4, 24, 4, 20));

        final JPanel outputSettingsPanel = new JPanel(tableLayout);
        outputSettingsPanel.setBorder(BorderFactory.createTitledBorder("Output Settings"));


        preserveResolutionCheckBox.addActionListener(e -> {
            if (preserveResolutionCheckBox.isSelected()) {
                outputParamButton.setEnabled(false);
            } else {
                outputParamButton.setEnabled(true);
            }

        });

        outputSettingsPanel.add(preserveResolutionCheckBox);

        outputSettingsPanel.add(includeTPcheck);

        outputParamButton = new JButton("Output Parameters...");
        outputParamButton.setEnabled(!preserveResolutionCheckBox.isSelected());
        outputParamButton.addActionListener(new OutputParamActionListener());
        outputSettingsPanel.add(outputParamButton);

        outputSettingsPanel.add(new JLabel("No-data value:"));


        outputSettingsPanel.add(noDataField);

        outputSettingsPanel.add(addDeltaBandsChecker);

        outputSettingsPanel.add(new JLabel("Resampling method:"));
        resampleComboBox.setPrototypeDisplayValue(RESAMPLING_IDENTIFIER[0]);
        outputSettingsPanel.add(resampleComboBox);


        transferValidPixelExpressionCheckBox = new JCheckBox("Retain valid pixel expression");
        transferValidPixelExpressionCheckBox.setSelected(true);
        outputSettingsPanel.add(transferValidPixelExpressionCheckBox);


        return outputSettingsPanel;
    }








    private JPanel createMaskSettingsPanel() {
        final TableLayout maskExpressionLayout = new TableLayout(3);
        maskExpressionLayout.setTablePadding(4, 4);
        maskExpressionLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        maskExpressionLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
        maskExpressionLayout.setTableWeightX(1.0);

        maskExpressionPanel = new JPanel(maskExpressionLayout);
        String maskExpressionToolTip = "Mask expression to apply to the source file(s)";

        editExpressionButton = new JButton("Edit ...");
        editExpressionButton.setPreferredSize(editExpressionButton.getPreferredSize());
        editExpressionButton.setMaximumSize(editExpressionButton.getPreferredSize());
        editExpressionButton.setMinimumSize(editExpressionButton.getPreferredSize());
        final Window parentWindow = SwingUtilities.getWindowAncestor(maskExpressionPanel);
        editExpressionButton.addActionListener(new EditExpressionActionListener(parentWindow));
        expressionArea = new JTextArea(3, 40);
        expressionArea.setLineWrap(true);

        JLabel maskExpressionLabel = new JLabel("Expression: ");
        maskExpressionPanel.add(maskExpressionLabel);
        maskExpressionPanel.add(new JScrollPane(expressionArea));
        maskExpressionPanel.add(editExpressionButton);

        maskExpressionPanel.setToolTipText(maskExpressionToolTip);
        maskExpressionLabel.setToolTipText(maskExpressionToolTip);
        editExpressionButton.setToolTipText(maskExpressionToolTip);
        expressionArea.setToolTipText(maskExpressionToolTip);

        applyValidPixelExpressionCheckBox = new JCheckBox("Apply source valid pixel expression");
        applyValidPixelExpressionCheckBox.setToolTipText("Applies source file valid pixel expression to masking criteria");
        applyValidPixelExpressionCheckBox.setSelected(true);

        final TableLayout layout = new TableLayout(1);
        layout.setTablePadding(4, 4);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
        layout.setTableWeightX(1.0);

        final JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createTitledBorder("Masking"));
        panel.add(maskExpressionPanel);
        panel.add(applyValidPixelExpressionCheckBox);

        return panel;
    }


    private class EditExpressionActionListener implements ActionListener {

        private final Window parentWindow;

        private EditExpressionActionListener(Window parentWindow) {
            this.parentWindow = parentWindow;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ProductExpressionPane pep = ProductExpressionPane.createBooleanExpressionPane(new Product[]{getSourceProduct()},
                    getSourceProduct(),
                    appContext.getPreferences());
            pep.setCode(expressionArea.getText());
            final int i = pep.showModalDialog(parentWindow, "Mask Expression Editor");
            if (i == ModalDialog.ID_OK) {
                expressionArea.setText(pep.getCode());
            }

        }
    }

    private void updateOutputParameterState() {
        outputParamButton.setEnabled(!preserveResolutionCheckBox.isSelected() && (crs != null));
        updateProductSize();
    }

    private void showWarningMessage(String message) {
        AbstractDialog.showWarningDialog(scrollPane, message, "Reprojection");
    }

    private class OutputParamActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                final Product sourceProduct = getSourceProduct();
                if (sourceProduct == null) {
                    showWarningMessage("Please select a product to reproject.\n");
                    return;
                }
                if (crs == null) {
                    showWarningMessage("Please specify a 'Coordinate Reference System' first.\n");
                    return;
                }
                OutputGeometryFormModel workCopy;
                if (outputGeometryModel != null) {
                    workCopy = new OutputGeometryFormModel(outputGeometryModel);
                } else {
                    // final Product collocationProduct = collocationCrsUI.getCollocationProduct();
                    // if (collocationCrsUI.getRadioButton().isSelected() && collocationProduct != null) {
                    //    workCopy = new OutputGeometryFormModel(sourceProduct, collocationProduct);
                    // } else {
                    workCopy = new OutputGeometryFormModel(sourceProduct, crs);
                    // }
                }
                final OutputGeometryForm form = new OutputGeometryForm(workCopy);
                final ModalDialog outputParametersDialog = new OutputParametersDialog(appContext.getApplicationWindow(),
                        sourceProduct, workCopy);
                outputParametersDialog.setContent(form);
                if (outputParametersDialog.show() == ModalDialog.ID_OK) {
                    outputGeometryModel = workCopy;
                    updateProductSize();
                }
            } catch (Exception e) {
                appContext.handleError("Could not create a 'Coordinate Reference System'.\n" +
                        e.getMessage(), e);
            }
        }

    }


    private class OutputParametersDialog extends ModalDialog {

        private static final String TITLE = "Output Parameters";

        private final Product sourceProduct;
        private final OutputGeometryFormModel outputGeometryFormModel;

        public OutputParametersDialog(Window parent, Product sourceProduct,
                                      OutputGeometryFormModel outputGeometryFormModel) {
            super(parent, TITLE, ModalDialog.ID_OK_CANCEL | ModalDialog.ID_RESET, null);
            this.sourceProduct = sourceProduct;
            this.outputGeometryFormModel = outputGeometryFormModel;
        }

        @Override
        protected void onReset() {
            // final Product collocationProduct = collocationCrsUI.getCollocationProduct();
            ImageGeometry imageGeometry;
            //if (collocationCrsUI.getRadioButton().isSelected() && collocationProduct != null) {
            //    imageGeometry = ImageGeometry.createCollocationTargetGeometry(sourceProduct, collocationProduct);
            // } else {
            imageGeometry = ImageGeometry.createTargetGeometry(sourceProduct, crs,
                    null, null, null, null,
                    null, null, null, null, null);
            //}
            outputGeometryFormModel.resetToDefaults(imageGeometry);
        }
    }
}
