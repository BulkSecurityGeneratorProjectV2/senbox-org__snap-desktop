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

package org.esa.snap.ui;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.util.math.Range;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Storm
 */
public class RGBImageProfilePaneTest {

    @Test
    public void testSelectProfile_1() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
                new RGBImageProfile("p2", new String[]{"", "", ""}, new String[]{"some_different_type", "*name_*3", null}),
                new RGBImageProfile("p3", new String[]{"", "", ""}, new String[]{"*me_ty*", "*name_*3", null}),
                new RGBImageProfile("p4", new String[]{"", "", ""}, new String[]{"*me_ty*", "*name_*3", "*s some*"}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
        assertSame(rgbImageProfiles[3], profile);   // all patterns match
    }

    @Test
    public void testSelectProfile_2() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
                new RGBImageProfile("p2", new String[]{"", "", ""}, new String[]{"some_different_type", "*name_*3", null}),
                new RGBImageProfile("p3", new String[]{"", "", ""}, new String[]{"*me_ty*", null, null}),
                new RGBImageProfile("p4", new String[]{"", "", ""}, new String[]{null, "*name_*3", "*s some*"}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
        assertSame(rgbImageProfiles[2], profile);     // type matches
    }

    @Test
    public void testSelectProfile_3() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
                new RGBImageProfile("p2", new String[]{"", "", ""}, new String[]{"some_different_type", "*name_*3", null}),
                new RGBImageProfile("p3", new String[]{"", "", ""}, new String[]{null, "*name_*3", null}),
                new RGBImageProfile("p4", new String[]{"", "", ""}, new String[]{null, "*name_*3", "*s some*"}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
        assertSame(rgbImageProfiles[3], profile);   // name and description match
    }

    @Test
    public void testSelectProfile_4() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
                new RGBImageProfile("p2", new String[]{"", "", ""}, new String[]{"some_different_type", "*name_*3", null}),
                new RGBImageProfile("p3", new String[]{"", "", ""}, new String[]{"strange type", "*name_*3", null}),
                new RGBImageProfile("p4", new String[]{"", "", ""}, new String[]{"strange type", "*name_*3", "*s some*"}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
        assertSame(rgbImageProfiles[3], profile);   // name and description match
    }

    @Test
    public void testSelectProfile_5() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
                new RGBImageProfile("p2", new String[]{"", "", ""}, new String[]{"some_different_type", "*name_*3", null}),
                new RGBImageProfile("p3", new String[]{"", "", ""}, new String[]{"*me_ty*", "*name_*3", null}),
                new RGBImageProfile("p4", new String[]{"", "", ""}, new String[]{"*me_ty*", "*name_*3", null}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
        assertSame(rgbImageProfiles[2], profile);   // equal, so earlier profile is chosen
    }

    @Test
    public void testSelectProfile_6() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[0];
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription("This is some description text.");
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNull(profile);
    }

    // Added by nf after NPE with Landsat 5 products
    @Test
    public void testNoDescriptionSet() {
        RGBImageProfile[] rgbImageProfiles = new RGBImageProfile[]{
                new RGBImageProfile("p1", new String[]{"", "", ""}, new String[]{"matches", "not at", "all"}),
        };
        Product product = new Product("some_name_123", "some_type_123", 1, 1);
        product.setDescription(null);
        RGBImageProfile profile = RGBImageProfilePane.findProfileForProductPattern(rgbImageProfiles, product);
        assertNotNull(profile);
    }

    @Test
    public void testRangeComponents_constructor() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();

        assertFalse(components.fixedRangeCheckBox.isSelected());

        assertFalse(components.minText.isEnabled());
        assertEquals("", components.minText.getText());
        assertFalse(components.minLabel.isEnabled());
        assertFalse(components.maxText.isEnabled());
        assertEquals("", components.maxText.getText());
        assertFalse(components.maxLabel.isEnabled());
    }

    @Test
    public void testRangeComponents_enableMinMax() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();

        components.enableMinMax(true);
        // changing this component triggers the actio - so it should not be altered during the call tb 2021-04-28
        assertFalse(components.fixedRangeCheckBox.isSelected());

        assertTrue(components.minText.isEnabled());
        assertEquals("", components.minText.getText());
        assertTrue(components.minLabel.isEnabled());
        assertTrue(components.maxText.isEnabled());
        assertEquals("", components.maxText.getText());
        assertTrue(components.maxLabel.isEnabled());
    }

    @Test
    public void testRangeComponents_setRange_validRange() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();

        components.set(new Range(0.87, 11.3));

        assertTrue(components.fixedRangeCheckBox.isSelected());
        assertTrue(components.minText.isEnabled());
        assertEquals("0.87", components.minText.getText());
        assertTrue(components.minLabel.isEnabled());
        assertTrue(components.maxText.isEnabled());
        assertEquals("11.3", components.maxText.getText());
        assertTrue(components.maxLabel.isEnabled());
    }

    @Test
    public void testRangeComponents_setRange_rangePartiallyFilled() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();

        components.set(new Range(0.88, Double.NaN));

        assertTrue(components.fixedRangeCheckBox.isSelected());
        assertTrue(components.minText.isEnabled());
        assertEquals("0.88", components.minText.getText());
        assertTrue(components.minLabel.isEnabled());
        assertTrue(components.maxText.isEnabled());
        assertEquals("", components.maxText.getText());
        assertTrue(components.maxLabel.isEnabled());

        components.set(new Range(Double.NaN, 9.8865));

        assertTrue(components.fixedRangeCheckBox.isSelected());
        assertTrue(components.minText.isEnabled());
        assertEquals("", components.minText.getText());
        assertTrue(components.minLabel.isEnabled());
        assertTrue(components.maxText.isEnabled());
        assertEquals("9.8865", components.maxText.getText());
        assertTrue(components.maxLabel.isEnabled());
    }

    @Test
    public void testRangeComponents_setRange_invalidRange() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();

        components.set(new Range(Double.NaN, Double.NaN));

        assertFalse(components.fixedRangeCheckBox.isSelected());
        assertFalse(components.minText.isEnabled());
        assertEquals("", components.minText.getText());
        assertFalse(components.minLabel.isEnabled());
        assertFalse(components.maxText.isEnabled());
        assertEquals("", components.maxText.getText());
        assertFalse(components.maxLabel.isEnabled());
    }

    @Test
    public void testRangeComponents_getRange() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();
        components.fixedRangeCheckBox.setSelected(true);

        components.minText.setText(" -1.76");
        components.maxText.setText(" 2.0076  ");

        final Range range = components.getRange();
        assertEquals(-1.76, range.getMin(), 1e-8);
        assertEquals(2.0076, range.getMax(), 1e-8);
    }

    @Test
    public void testRangeComponents_getRange_unselected() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();
        components.fixedRangeCheckBox.setSelected(false);

        components.minText.setText(" -1.76");
        components.maxText.setText(" 2.0076  ");

        final Range range = components.getRange();
        assertEquals(Double.NaN, range.getMin(), 1e-8);
        assertEquals(Double.NaN, range.getMax(), 1e-8);
    }

    @Test
    public void testRangeComponents_getRange_emptyFields() {
        final RGBImageProfilePane.RangeComponents components = new RGBImageProfilePane.RangeComponents();
        components.fixedRangeCheckBox.setSelected(true);

        components.minText.setText("");
        components.maxText.setText("6.8");

        Range range = components.getRange();
        assertEquals(Double.NaN, range.getMin(), 1e-8);
        assertEquals(6.8, range.getMax(), 1e-8);

        components.minText.setText("0.004");
        components.maxText.setText("");

        range = components.getRange();
        assertEquals(0.004, range.getMin(), 1e-8);
        assertEquals(Double.NaN, range.getMax(), 1e-8);
    }
}
