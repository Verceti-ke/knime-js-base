/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   16 May 2019 (albrecht): created
 */
package org.knime.js.base.template;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;
import org.knime.js.base.node.viz.generic3.GenericJSViewConfig;

/**
 * The dialog which is displayed when adding a new template
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("serial")
public final class AddTemplateDialog extends JDialog {

    private JSTemplate m_result;
    private JComboBox<String> m_category;
    private final JButton m_ok;
    private JTextField m_name;
    private JTextArea m_description;

    private final GenericJSViewConfig m_settings;
    private final Class<?> m_metaCategory;

    /**
     * Create a new dialog.
     *
     * @param parent frame who owns this dialog
     * @param settings the settings to create the template
     * @param metaCategory the meta category
     */
    public AddTemplateDialog(final Frame parent, final GenericJSViewConfig settings, final Class<?> metaCategory) {
        super(parent, true);

        m_settings = settings;
        m_metaCategory = metaCategory;

        setTitle("Add a JavaScript view template");
        // instantiate the components of the dialog
        final JPanel p = createPanel();

        // the OK and Cancel button
        final JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        m_ok = new JButton("OK");
        // add action listener
        m_ok.addActionListener(e -> onOK());
        m_ok.setEnabled(false);
        final JButton cancel = new JButton("Cancel");
        // add action listener
        cancel.addActionListener(e -> onCancel());
        control.add(m_ok);
        control.add(cancel);

        m_name.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                m_ok.setEnabled(StringUtils.isNotBlank(m_name.getText()));
            }
        });

        // add dialog and control panel to the content pane
        final Container cont = getContentPane();
        cont.setLayout(new BorderLayout());
        cont.add(p, BorderLayout.CENTER);
        cont.add(control, BorderLayout.SOUTH);

        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JPanel createPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.BASELINE;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        final Insets leftInsets = new Insets(3, 8, 3, 8);
        final Insets rightInsets = new Insets(3, 0, 3, 8);
        final Insets leftCategoryInsets = new Insets(11, 8, 3, 8);
        final Insets rightCategoryInsets = new Insets(11, 0, 3, 8);

        c.gridx = 0;
        c.insets = leftCategoryInsets;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        final TemplateProvider provider = TemplateProvider.getDefault();
        m_category = new JComboBox<>();
        m_category.setEditable(true);
        final Set<String> categories = provider.getCategories(Collections.<Class<?>> singletonList(m_metaCategory));
        categories.remove(TemplateProvider.ALL_CATEGORY);
        for (final String category : categories) {
            m_category.addItem(category);
        }
        p.add(new JLabel("Category:"), c);

        c.gridx++;
        c.insets = rightCategoryInsets;
        p.add(m_category, c);

        c.gridy++;
        c.gridx = 0;
        c.insets = leftInsets;
        p.add(new JLabel("Title:"), c);

        c.gridx++;
        c.insets = rightInsets;
        m_name = new JTextField("");
        p.add(m_name, c);

        c.gridy++;
        c.gridx = 0;
        c.insets = leftInsets;

        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        m_description = new JTextArea();
        final JScrollPane descScroller = new JScrollPane(m_description);
        descScroller.setBorder(BorderFactory.createTitledBorder("Description"));
        p.add(descScroller, c);

        p.setPreferredSize(new Dimension(400, 300));

        return p;
    }

    /** Save settings in field m_result. */
    private JSTemplate takeOverSettings() {
        final JSTemplate template = m_settings.createTemplate(m_metaCategory);
        template.setCategory((String)m_category.getSelectedItem());
        template.setName(m_name.getText());
        template.setDescription(m_description.getText());
        return template;
    }

    /**
     * Called when user presses the ok button.
     */
    void onOK() {
        m_result = takeOverSettings();
        if (m_result != null) {
            shutDown();
        }
    }

    /**
     * Called when user presses the cancel button or closes the window.
     */
    void onCancel() {
        m_result = null;
        shutDown();
    }

    /** Hides the dialog. */
    private void shutDown() {
        setVisible(false);
    }



    /**
     * Opens a Dialog to receive user settings. If the user cancels the dialog <code>null</code> will be returned. If
     * okay is pressed, the settings from the dialog will be stored in a new {@link JSTemplate} object.<br>
     * If user's settings are incorrect an error dialog pops up and the user values are discarded.
     *
     * @param parent frame who owns this dialog
     * @param settings the settings to create the template
     * @param metaCategory the meta category
     * @return new template are null in case of cancellation
     */
    public static JSTemplate openUserDialog(final Frame parent, final GenericJSViewConfig settings,
        final Class<?> metaCategory) {
        final AddTemplateDialog dialog = new AddTemplateDialog(parent, settings, metaCategory);
        return dialog.showDialog();
    }

    /**
     * Shows the dialog and waits for it to return. If the user pressed Ok it returns the OutCol definition
     */
    private JSTemplate showDialog() {
        pack();
        centerDialog();

        setVisible(true);
        /* ---- won't come back before dialog is disposed -------- */
        /* ---- on Ok we transfer the settings into the m_result -- */
        return m_result;
    }

    /**
     * Sets this dialog in the center of the screen observing the current screen size.
     */
    private void centerDialog() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = getSize();
        setBounds(Math.max(0, (screenSize.width - size.width) / 2), Math.max(0, (screenSize.height - size.height) / 2),
            Math.min(screenSize.width, size.width), Math.min(screenSize.height, size.height));
    }

}
