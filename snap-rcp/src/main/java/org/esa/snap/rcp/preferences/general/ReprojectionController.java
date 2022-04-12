

/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.rcp.preferences.general;

        import com.bc.ceres.binding.Property;
        import com.bc.ceres.binding.PropertyDescriptor;
        import com.bc.ceres.binding.PropertySet;
        import com.bc.ceres.binding.ValidationException;
        import com.bc.ceres.swing.TableLayout;
        import com.bc.ceres.swing.binding.BindingContext;
        import com.bc.ceres.swing.binding.Enablement;
        import com.bc.ceres.swing.binding.PropertyEditorRegistry;
        import com.bc.ceres.swing.binding.PropertyPane;
        import org.esa.snap.rcp.preferences.DefaultConfigController;
        import org.esa.snap.rcp.preferences.Preference;
        import org.netbeans.spi.options.OptionsPanelController;
        import org.openide.util.HelpCtx;

        import javax.swing.*;
        import java.awt.*;

/**
 * Panel handling general layer preferences. Sub-panel of the "Layer"-panel.
 *
 * @author Daniel Knowles (NASA)
 */
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_Reprojection=" + "Reprojection",
        "Options_Keywords_Reprojection=layer, general"
})
@OptionsPanelController.SubRegistration(location = "GeneralPreferences",
        displayName = "#Options_DisplayName_Reprojection",
        keywords = "#Options_Keywords_Reprojection",
        keywordsCategory = "reprojection, layer",
        id = "reprojectionController",
        position = 3)


public final class ReprojectionController extends DefaultConfigController {


    // Preferences property prefix
    private static final String PROPERTY_ROOT_KEY = "reprojection";

    // Masking

    private static final String PROPERTY_MASKING_KEY_SUFFIX = PROPERTY_ROOT_KEY + ".masking";

    public static final String PROPERTY_MASKING_KEY_SECTION_KEY = PROPERTY_MASKING_KEY_SUFFIX + ".section";
    public static final String PROPERTY_MASKING_KEY_SECTION_LABEL = "Masking";
    public static final String PROPERTY_MASKING_KEY_SECTION_TOOLTIP = "Masking options";

    public static final String MASK_EXPRESSION_KEY = PROPERTY_MASKING_KEY_SUFFIX + ".mask.expression";
    public static final String MASK_EXPRESSION_LABEL = "Expression";
    public static final String MASK_EXPRESSION_TOOLTIP = "Mask expression to apply to the source file(s)";
    public static String MASK_EXPRESSION_DEFAULT = "";

    public static final String APPLY_VALID_PIXEL_EXPRESSION_KEY = PROPERTY_MASKING_KEY_SUFFIX + ".apply.valid.pixel.expression";
    public static final String APPLY_VALID_PIXEL_EXPRESSION_LABEL = "Apply source valid pixel expression";
    public static final String APPLY_VALID_PIXEL_EXPRESSION_TOOLTIP = "Applies source file valid pixel expression to masking criteria";
    public static boolean APPLY_VALID_PIXEL_EXPRESSION_DEFAULT = true;



    // Restore to defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = PROPERTY_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_NAME = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (" + "Reprojection" + " Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all " + " Reprojection preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;





    Property restoreDefaults;

    boolean propertyValueChangeEventsEnabled = true;


    Enablement enablementGeneralPalette;
    Enablement enablementGeneralRange;
    Enablement enablementGeneralLog;




    protected PropertySet createPropertySet() {
        return createPropertySet(new GeneralLayerBean());
    }


    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("colorManipulationPreferences");
    }

    @Override
    protected JPanel createPanel(BindingContext context) {


        initPropertyDefaults(context, PROPERTY_MASKING_KEY_SECTION_KEY, true);
        initPropertyDefaults(context, MASK_EXPRESSION_KEY, MASK_EXPRESSION_DEFAULT);
        initPropertyDefaults(context, APPLY_VALID_PIXEL_EXPRESSION_KEY, APPLY_VALID_PIXEL_EXPRESSION_DEFAULT);




        restoreDefaults =  initPropertyDefaults(context, PROPERTY_RESTORE_DEFAULTS_NAME, PROPERTY_RESTORE_DEFAULTS_DEFAULT);




        //
        // Create UI
        //

        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
        tableLayout.setTablePadding(new Insets(4, 10, 0, 0));
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setColumnWeightX(1, 1.0);

        JPanel pageUI = new JPanel(tableLayout);

        PropertyEditorRegistry registry = PropertyEditorRegistry.getInstance();

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        int currRow = 0;
        for (Property property : properties) {
            PropertyDescriptor descriptor = property.getDescriptor();
            PropertyPane.addComponent(currRow, tableLayout, pageUI, context, registry, descriptor);
            currRow++;
        }

        pageUI.add(tableLayout.createVerticalSpacer());

        JPanel parent = new JPanel(new BorderLayout());
        parent.add(pageUI, BorderLayout.CENTER);
        parent.add(Box.createHorizontalStrut(50), BorderLayout.EAST);
        return parent;
    }



    @Override
    protected void configure(BindingContext context) {

        configureGeneralCustomEnablement(context);

        // Handle resetDefaults events - set all other components to defaults
        restoreDefaults.addPropertyChangeListener(evt -> {
            handleRestoreDefaults(context);
        });


        // Add listeners to all components in order to uncheck restoreDefaults checkbox accordingly

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        for (Property property : properties) {
            if (property != restoreDefaults) {
                property.addPropertyChangeListener(evt -> {
                    handlePreferencesPropertyValueChange(context);
                });
            }
        }
    }






    /**
     * Test all properties to determine whether the current value is the default value
     *
     * @param context
     * @return
     * @author Daniel Knowles
     */
    private boolean isDefaults(BindingContext context) {

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        for (Property property : properties) {
            if (property != restoreDefaults && property.getDescriptor().getDefaultValue() != null)
                if (!property.getValue().equals(property.getDescriptor().getDefaultValue())) {
                    return false;
                }
        }

        return true;
    }


    /**
     * Handles the restore defaults action
     *
     * @param context
     * @author Daniel Knowles
     */
    private void handleRestoreDefaults(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            try {
                if (restoreDefaults.getValue()) {

                    PropertySet propertyContainer = context.getPropertySet();
                    Property[] properties = propertyContainer.getProperties();

                    for (Property property : properties) {
                        if (property != restoreDefaults && property.getDescriptor().getDefaultValue() != null)
                            property.setValue(property.getDescriptor().getDefaultValue());
                    }
                }
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;

            context.setComponentsEnabled(PROPERTY_RESTORE_DEFAULTS_NAME, false);
        }
    }


    /**
     * Set restoreDefault component because a property has changed
     * @param context
     * @author Daniel Knowles
     */
    private void handlePreferencesPropertyValueChange(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            try {
                restoreDefaults.setValue(isDefaults(context));
                context.setComponentsEnabled(PROPERTY_RESTORE_DEFAULTS_NAME, !isDefaults(context));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;
        }
    }


    /**
     * Initialize the property descriptor default value
     *
     * @param context
     * @param propertyName
     * @param propertyDefault
     * @return
     * @author Daniel Knowles
     */
    private Property initPropertyDefaults(BindingContext context, String propertyName, Object propertyDefault) {

        Property property = context.getPropertySet().getProperty(propertyName);

        property.getDescriptor().setDefaultValue(propertyDefault);

        return property;
    }



    /**
     * Configure enablement of the components tied to PROPERTY_GENERAL_CUSTOM_KEY
     *
     * @param context
     * @author Daniel Knowles
     */
    private void configureGeneralCustomEnablement(BindingContext context) {
//
//
//        enablementGeneralPalette = context.bindEnabledState(ColorManipulationDefaults.PROPERTY_GENERAL_PALETTE_KEY, true,
//                ColorManipulationDefaults.PROPERTY_GENERAL_CUSTOM_KEY, true);
//
//        enablementGeneralRange = context.bindEnabledState(ColorManipulationDefaults.PROPERTY_GENERAL_RANGE_KEY, true,
//                ColorManipulationDefaults.PROPERTY_GENERAL_CUSTOM_KEY, true);
//
//        enablementGeneralLog = context.bindEnabledState(ColorManipulationDefaults.PROPERTY_GENERAL_LOG_KEY, true,
//                ColorManipulationDefaults.PROPERTY_GENERAL_CUSTOM_KEY, true);
//
//
//
//        // handle it the first time so bound properties get properly enabled
////        handleGeneralCustom();
//        enablementGeneralPalette.apply();
//        enablementGeneralRange.apply();
//        enablementGeneralLog.apply();
    }
//
//    /**
//     * Handles enablement of the components
//     *
//     * @author Daniel Knowles
//     */
//    private void handleGeneralCustom() {
//        enablementGeneralPalette.apply();
//        enablementGeneralRange.apply();
//        enablementGeneralLog.apply();
//    }
//






    @SuppressWarnings("UnusedDeclaration")
    static class GeneralLayerBean {

        // Masking

        @Preference(label = PROPERTY_MASKING_KEY_SECTION_LABEL,
                key = PROPERTY_MASKING_KEY_SECTION_KEY,
                description = PROPERTY_MASKING_KEY_SECTION_TOOLTIP)
        boolean defaultPaletteSection = true;

        @Preference(label = MASK_EXPRESSION_LABEL,
                key = MASK_EXPRESSION_KEY,
                description = MASK_EXPRESSION_TOOLTIP)
        String maskExpression = MASK_EXPRESSION_DEFAULT;

        @Preference(label = APPLY_VALID_PIXEL_EXPRESSION_LABEL,
                key = APPLY_VALID_PIXEL_EXPRESSION_KEY,
                description = APPLY_VALID_PIXEL_EXPRESSION_TOOLTIP)
        boolean grayScaleCpd = APPLY_VALID_PIXEL_EXPRESSION_DEFAULT;





        // Restore Defaults

        @Preference(label = PROPERTY_RESTORE_SECTION_LABEL,
                key = PROPERTY_RESTORE_SECTION_KEY,
                description = PROPERTY_RESTORE_SECTION_TOOLTIP)
        boolean restoreDefaultsSection = true;

        @Preference(label = PROPERTY_RESTORE_DEFAULTS_LABEL,
                key = PROPERTY_RESTORE_DEFAULTS_NAME,
                description = PROPERTY_RESTORE_DEFAULTS_TOOLTIP)
        boolean restoreDefaults = PROPERTY_RESTORE_DEFAULTS_DEFAULT;
    }

}

