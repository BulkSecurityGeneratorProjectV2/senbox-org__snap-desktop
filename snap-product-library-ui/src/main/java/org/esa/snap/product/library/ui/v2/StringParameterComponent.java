package org.esa.snap.product.library.ui.v2;

import javax.swing.JTextField;
import java.awt.Dimension;

/**
 * Created by jcoravu on 7/8/2019.
 */
public class StringParameterComponent extends AbstractParameterComponent<String> {

    private final JTextField component;

    public StringParameterComponent(String parameterName, String defaultValue, String parameterLabelText, int textFieldPreferredHeight) {
        super(parameterName, parameterLabelText);

        this.component = new JTextField();

        Dimension preferredSize = this.component.getPreferredSize();
        preferredSize.height = textFieldPreferredHeight;
        this.component.setPreferredSize(preferredSize);
        this.component.setMinimumSize(preferredSize);

        if (defaultValue != null) {
            this.component.setText(defaultValue);
        }
    }

    @Override
    public JTextField getComponent() {
        return this.component;
    }

    @Override
    public String getParameterValue() {
        String value = this.component.getText().trim();
        return (value.equals("") ? null : value);
    }
}
