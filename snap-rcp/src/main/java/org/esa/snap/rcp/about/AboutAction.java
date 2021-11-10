/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.esa.snap.rcp.about;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Displays the {@link AboutPanel} in a modal dialog.
 *
 * @author Norman Fomferra
 */
@ActionID(category = "Help", id = "org.esa.snap.rcp.about.AboutAction" )
//@ActionRegistration(displayName = "#CTL_AboutAction_Name" )
@ActionReference(path = "Menu/Help", position = 1600, separatorBefore = 1550)
//@Messages({
//        "CTL_AboutAction_Name=About SeaDAS...",
////        "CTL_AboutAction_Title=About SeaDAS",
//})
public final class AboutAction implements ActionListener {
    public static AboutAction create(Map<String, Object> configuration) {
        AboutAction aboutAction = new AboutAction();
        return aboutAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getBundle("org.netbeans.core.ui.Bundle").getString("CTL_AboutAction_Title"), true);
        dialog.setContentPane(new AboutPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

}
