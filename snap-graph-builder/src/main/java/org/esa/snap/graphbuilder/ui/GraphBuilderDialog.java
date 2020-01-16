package org.esa.snap.graphbuilder.ui;

import java.awt.Dimension;

import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.ModelessDialog;

public class GraphBuilderDialog extends ModelessDialog {
 
    public GraphBuilderDialog(final AppContext theAppContext, final String title, final String helpID){
        this(theAppContext, title, helpID, true);
    }

    public GraphBuilderDialog(final AppContext theAppContext, final String title, final String helpID, final boolean allowGraphBuilding) {
        super(theAppContext.getApplicationWindow(), title, 0, helpID);
        super.getJDialog().setMinimumSize(new Dimension(1200, 800));

        this.setContent(new GraphBuilder(theAppContext));
    }
}