/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>
 *
 */
package de.nomagic.WizardDialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Translator.Translator;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class BaseWindow implements ActionListener, Runnable
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private JFrame Dialog;
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;
    private Vector<WizardSlide> SlideHistory;
    private WizardSlide curSlide;
    private ImageIcon nextIcon;
    private ImageIcon finishIcon;
    private JPanel SlidePanel;
    private CardLayout SlideLayout;
    private Translator t;
    private DataStore ds = new DataStore();
    private CancelAction ca = null;
    /** The String-based action command for the 'Next' button. */
    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    /** The String-based action command for the 'Back' button. */
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    /** The String-based action command for the 'Cancel' button. */
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";

    public BaseWindow(Translator t, WizardSlide firstSlide,  CancelAction ca)
    {
        this.t = t;
        this.ca = ca;
        SlideHistory = new Vector<WizardSlide>();
        Dialog =  new JFrame();
        Dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dialog.setTitle(t.t("Base_WindowTitle"));

        SlidePanel = new JPanel();
        SlideLayout = new CardLayout();

        SlidePanel.setLayout(SlideLayout);
        SlidePanel.setOpaque(true);
        SlidePanel.setBackground(Color.RED);
        SlidePanel.setMinimumSize(new Dimension(300, 300));

        curSlide = firstSlide;

        final JLabel iconLabel = new JLabel();
        final ImageIcon icon = new ImageIcon("res/clouds.jpg");
        if(-1 ==  icon.getIconHeight())
        {
            log.error("Could not load the background Image");
            iconLabel.setText("Image Missing !");
        }
        else
        {
            iconLabel.setIcon(icon);
        }
        iconLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        iconLabel.setOpaque(true);

        final Container contPane = Dialog.getContentPane();
        if(null == contPane)
        {
            log.error("Content Pane is Null !");
            return;
        }
        contPane.setLayout(new BorderLayout());

        contPane.add(iconLabel, BorderLayout.WEST);
        contPane.add(createButtonPane(), BorderLayout.SOUTH);
        contPane.add(SlidePanel, BorderLayout.EAST);
    }

    private JPanel createButtonPane()
    {
        final JPanel buttonPanel = new JPanel();
        final Box buttonBox = new Box(BoxLayout.X_AXIS);
        final JSeparator separator = new JSeparator();
        nextIcon = new ImageIcon("res/nextIcon.gif");
        finishIcon = new ImageIcon("res/finishIcon.gif");
        backButton = new JButton(new ImageIcon("res/backIcon.gif"));
        nextButton = new JButton(nextIcon);
        cancelButton = new JButton(new ImageIcon("res/cancelIcon.gif"));

        backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
        backButton.setText(t.t("Base_BackButton"));
        nextButton.setText(t.t("Base_NextButton"));
        cancelButton.setText(t.t("Base_CancelButton"));

        backButton.addActionListener(this);
        nextButton.addActionListener(this);
        cancelButton.addActionListener(this);
        backButton.setOpaque(true);
        nextButton.setOpaque(true);
        cancelButton.setOpaque(true);
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);
        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);
        buttonBox.setOpaque(true);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
        buttonPanel.setOpaque(true);
        return buttonPanel;
    }


    public final void run()
    {
        setCurrentPanel(curSlide);
        Dialog.pack();
        Dialog.setVisible(true);
    }

    private void setCurrentPanel(final WizardSlide cs)
    {
        log.debug("setting current Panel to " + cs.getName());
        curSlide = cs;
        // Next or Finish on last Slide
        if(false == curSlide.hasNextSlide())
        {
            nextButton.setIcon(finishIcon);
            nextButton.setText(t.t("Base_FinishButton"));
        }
        else
        {
            nextButton.setIcon(nextIcon);
            nextButton.setText(t.t("Base_NextButton"));
        }
        // Back Enable / Disabled on first Slide
        if(1 > SlideHistory.size())
        {
            backButton.setEnabled(false);
        }
        else
        {
            backButton.setEnabled(true);
        }
        SlideLayout.show(SlidePanel, cs.getName());
        ds = curSlide.actionOnShow(ds);
        SlidePanel.revalidate();
        SlidePanel.repaint();
    }

    @Override
    public final void actionPerformed(final ActionEvent e)
    {
        if (e.getActionCommand().equals(CANCEL_BUTTON_ACTION_COMMAND))
        {
            cancelButtonPressed();
        }
        else if (e.getActionCommand().equals(BACK_BUTTON_ACTION_COMMAND))
        {
            backButtonPressed();
        }
        else if (e.getActionCommand().equals(NEXT_BUTTON_ACTION_COMMAND))
        {
            nextButtonPressed();
        }
        else
        {
            log.error(e.getActionCommand());
        }
    }

    private void cancelButtonPressed()
    {
        if(null != ca)
        {
            ca.userClickedCancel(ds);
        }
        Dialog.dispose();
    }

    private void nextButtonPressed()
    {
        ds = curSlide.actionOnClose(ds);
        final WizardSlide next = curSlide.getNextSlide();
        if(null != next)
        {
            SlideHistory.add(curSlide);
            setCurrentPanel(next);
        }
        else
        {
            // finished Dialog
            Dialog.dispose();
        }
    }

    private void backButtonPressed()
    {
        WizardSlide last = null;
        try
        {
            last = SlideHistory.lastElement();
            SlideHistory.remove(SlideHistory.size() -1);
        }
        catch(final NoSuchElementException e)
        {
            // No lastSlide in Vector
        }
        catch(final ArrayIndexOutOfBoundsException e)
        {
            // could not remove size -1
        }
        if(null != last)
        {
            setCurrentPanel(last);
        }
    }

    public void addSlide(Component slide, String Name)
    {
        SlidePanel.add(slide, Name);
    }

    public void setDatatStore(DataStore ds)
    {
        this.ds = ds;
    }

}
