// For conditions of distribution and use, see copyright notice in Main.java

// Pufferfish dashboard.

package morphognosis.pufferfish;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import morphognosis.MorphognosticDisplay;

public class PufferfishDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;

   // Targets.
   Pufferfish  pufferfish;
   NestDisplay nestDisplay;

   // Constructor.
   public PufferfishDashboard(Pufferfish pufferfish, NestDisplay nestDisplay)
   {
      this.pufferfish  = pufferfish;
      this.nestDisplay = nestDisplay;

      setTitle("Pufferfish");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());
      sensorsResponse = new SensorsResponsePanel();
      basePanel.add(sensorsResponse, BorderLayout.NORTH);
      driver = new DriverPanel();
      basePanel.add(driver, BorderLayout.CENTER);
      morphognostic = new MorphognosticDisplay(0, pufferfish.morphognostic);
      basePanel.add(morphognostic, BorderLayout.SOUTH);
      pack();
      setCenterLocation();
      setVisible(false);
      update();
   }


   void setCenterLocation()
   {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int       w   = getSize().width;
      int       h   = getSize().height;
      int       x   = (dim.width - w) / 2;
      int       y   = (dim.height - h) / 2;

      setLocation(x, y);
   }


   // Update dashboard.
   void update()
   {
      int x, y, cx, cy;
      int width  = nestDisplay.nest.size.width;
      int height = nestDisplay.nest.size.height;

      String elevationString = "";
      String s = "";

      cx = pufferfish.x;
      cy = pufferfish.y;
      for (int i = 0; i < Pufferfish.NUM_SENSORS; i++)
      {
         x = cx;
         y = cy;
         switch (i)
         {
         case 0:
            x--;
            if (x < 0) { x += width; }
            y = ((y + 1) % height);
            s = ",";
            break;

         case 1:
            y = ((y + 1) % height);
            s = ",";
            break;

         case 2:
            x = ((x + 1) % width);
            y = ((y + 1) % height);
            s = "/";
            break;

         case 3:
            x--;
            if (x < 0) { x += width; }
            s = ",";
            break;

         case 4:
            s = ",";
            break;

         case 5:
            x = ((x + 1) % width);
            s = "/";
            break;

         case 6:
            x--;
            if (x < 0) { x += width; }
            y--;
            if (y < 0) { y += height; }
            s = ",";
            break;

         case 7:
            y--;
            if (y < 0) { y += height; }
            s = ",";
            break;

         case 8:
            x = ((x + 1) % width);
            y--;
            if (y < 0) { y += height; }
            break;
         }
         elevationString += nestDisplay.nest.cells[x][y][0] + s;
      }
      setSensors(elevationString);
      if (pufferfish.response == Pufferfish.WAIT)
      {
         setResponse("wait");
      }
      else if (pufferfish.response == Pufferfish.FORWARD)
      {
         setResponse("forward");
      }
      else if (pufferfish.response == Pufferfish.TURN_LEFT)
      {
         setResponse("turn left");
      }
      else if (pufferfish.response == Pufferfish.TURN_RIGHT)
      {
         setResponse("turn right");
      }
      else if (pufferfish.response == Pufferfish.RAISE)
      {
         setResponse("raise surface");
      }
      else if (pufferfish.response == Pufferfish.LOWER)
      {
         setResponse("lower surface");
      }
      else
      {
         setResponse("");
      }
      setDriverChoice(pufferfish.driver);
   }


   // Open the dashboard.
   void open()
   {
      setVisible(true);
   }


   // Close the dashboard.
   void close()
   {
      morphognostic.close();
      setVisible(false);
   }


   // Set sensors display.
   void setSensors(String elevationString)
   {
      sensorsResponse.elevationText.setText(elevationString);
   }


   // Set response display.
   void setResponse(String responseString)
   {
      sensorsResponse.responseText.setText(responseString);
   }


   // Sensors/Response panel.
   class SensorsResponsePanel extends JPanel
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JTextField elevationText;
      JTextField responseText;

      // Constructor.
      public SensorsResponsePanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      "Sensors/Response"));
         JPanel sensorsPanel = new JPanel();
         sensorsPanel.setLayout(new BorderLayout());
         add(sensorsPanel, BorderLayout.NORTH);
         JPanel elevationPanel = new JPanel();
         elevationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(elevationPanel, BorderLayout.CENTER);
         elevationPanel.add(new JLabel("Elevations:"));
         elevationText = new JTextField(20);
         elevationText.setEditable(false);
         elevationPanel.add(elevationText);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(responsePanel, BorderLayout.SOUTH);
         responsePanel.add(new JLabel("Response:"));
         responseText = new JTextField(10);
         responseText.setEditable(false);
         responsePanel.add(responseText);
      }
   }

   // Get driver choice.
   int getDriverChoice()
   {
      return(driver.driverChoice.getSelectedIndex());
   }


   // Set driver choice.
   void setDriverChoice(int driverChoice)
   {
      driver.driverChoice.select(driverChoice);
   }


   // Driver panel.
   class DriverPanel extends JPanel implements ItemListener, ActionListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      Choice   driverChoice;
      JButton  forwardButton;
      JButton  turnLeftButton;
      JButton  turnRightButton;
      JButton  raiseSurfaceButton;
      JButton  lowerSurfaceButton;
      Checkbox trainNNcheck;

      // Constructor.
      public DriverPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Driver"));
         JPanel driverPanel = new JPanel();
         driverPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(driverPanel, BorderLayout.NORTH);
         driverPanel.add(new JLabel("Driver:"));
         driverChoice = new Choice();
         driverPanel.add(driverChoice);
         driverChoice.add("metamorphDB");
         driverChoice.add("metamorphNN");
         driverChoice.add("autopilot");
         driverChoice.add("manual");
         driverChoice.addItemListener(this);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout());
         add(responsePanel, BorderLayout.CENTER);
         forwardButton = new JButton("Forward");
         forwardButton.addActionListener(this);
         responsePanel.add(forwardButton);
         turnLeftButton = new JButton("Turn left");
         turnLeftButton.addActionListener(this);
         responsePanel.add(turnLeftButton);
         turnRightButton = new JButton("Turn right");
         turnRightButton.addActionListener(this);
         responsePanel.add(turnRightButton);
         raiseSurfaceButton = new JButton("Raise");
         raiseSurfaceButton.addActionListener(this);
         responsePanel.add(raiseSurfaceButton);
         lowerSurfaceButton = new JButton("Lower");
         lowerSurfaceButton.addActionListener(this);
         responsePanel.add(lowerSurfaceButton);
         JPanel trainNNpanel = new JPanel();
         trainNNpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(trainNNpanel, BorderLayout.SOUTH);
         trainNNpanel.add(new JLabel("Train NN:"));
         trainNNcheck = new Checkbox();
         trainNNcheck.setState(false);
         trainNNcheck.addItemListener(this);
         trainNNpanel.add(trainNNcheck);
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Choice && ((Choice)source == driverChoice))
         {
            pufferfish.driver = driverChoice.getSelectedIndex();
            pufferfish.initAutopilot();
            return;
         }
         if (source instanceof Checkbox && ((Checkbox)source == trainNNcheck))
         {
            if (trainNNcheck.getState())
            {
               try
               {
                  pufferfish.createMetamorphNN();
               }
               catch (Exception e)
               {
                  nestDisplay.controls.messageText.setText("Cannot train metamorph NN: " + e.getMessage());
               }
               trainNNcheck.setState(false);
            }
            return;
         }
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == forwardButton)
         {
            pufferfish.driverResponse = Pufferfish.FORWARD;
            return;
         }

         if ((JButton)evt.getSource() == turnLeftButton)
         {
            pufferfish.driverResponse = Pufferfish.TURN_LEFT;
            return;
         }

         if ((JButton)evt.getSource() == turnRightButton)
         {
            pufferfish.driverResponse = Pufferfish.TURN_RIGHT;
            return;
         }

         if ((JButton)evt.getSource() == raiseSurfaceButton)
         {
            pufferfish.driverResponse = Pufferfish.RAISE;
            return;
         }

         if ((JButton)evt.getSource() == lowerSurfaceButton)
         {
            pufferfish.driverResponse = Pufferfish.LOWER;
            return;
         }
      }
   }
}
