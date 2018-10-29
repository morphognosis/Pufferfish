// For conditions of distribution and use, see copyright notice in Main.java

// Nest editor.

package morphognosis.pufferfish;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.SecureRandom;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class NestEditor extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java morphognosis.pufferfish.NestEditor\n" +
      "      -nestDimensions <width> <height>\n" +
      "     [-maxElevation <quantity> (default=" + Nest.MAX_ELEVATION + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + Main.DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-save <file name>]\n" +
      "  Resume run:\n" +
      "    java morphognosis.pufferfish.NestEditor\n" +
      "      -load <file name>\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n";

   // Nest.
   Nest nest;

   // Display size.
   public static final Dimension DISPLAY_SIZE = new Dimension(600, 600);

   // Display update frequency (ms).
   public static final int DISPLAY_DELAY = 50;

   // Nest display.
   NestDisplay display;

   // Quit.
   boolean quit;

   // Random numbers.
   SecureRandom random;
   int          randomSeed;

   // Constructors.
   public NestEditor(Nest nest)
   {
      this.nest = nest;

      // Random numbers.
      random = new SecureRandom();
      random.setSeed(randomSeed);

      // Set up display.
      setTitle("Pufferfish nest editor");
      quit = false;
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e)
                           {
                              quit = true;
                           }
                        }
                        );
      setBounds(0, 0, DISPLAY_SIZE.width, DISPLAY_SIZE.height);
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());

      // Create display.
      Dimension displaySize = new Dimension(DISPLAY_SIZE.width, DISPLAY_SIZE.height);
      display = new NestDisplay(displaySize);
      basePanel.add(display, BorderLayout.CENTER);

      // Make display visible.
      pack();
      setCenterLocation();
      setVisible(true);
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


   // Run display.
   public void run()
   {
      while (!quit)
      {
         // Update display.
         display.update();

         try
         {
            Thread.sleep(DISPLAY_DELAY);
         }
         catch (InterruptedException e)
         {
            break;
         }
      }
   }


   // Nest display.
   public class NestDisplay extends Canvas implements MouseWheelListener
   {
      private static final long serialVersionUID = 0L;

      // Buffered display.
      Dimension canvasSize;
      Graphics  graphics;
      Image     image;
      Graphics  imageGraphics;

      // Font.
      Font        font = new Font("Helvetica", Font.BOLD, 16);
      FontMetrics fontMetrics;
      int         fontAscent;
      int         fontWidth;
      int         fontHeight;

      // Fish elevation for "plowing" surface.
      int fishElevation;

      // Fish information message switch.
      private boolean fishInfoMsg;

      // Cell size.
      int   width, height;
      float cellWidth;
      float cellHeight;

      // Selected cell.
      int selectedX, selectedY;

      // Constructor.
      public NestDisplay(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());
         addMouseWheelListener(this);

         // Set fish elevation.
         fishElevation = 0;
         fishInfoMsg   = true;

         // Compute sizes.
         width      = nest.size.width;
         height     = nest.size.height;
         cellWidth  = (float)canvasSize.width / (float)width;
         cellHeight = (float)canvasSize.height / (float)height;

         // Initialize selected cell.
         selectedX = selectedY = -1;
      }


      // Update display.
      void update()
      {
         int x, y, x2, y2;

         if (graphics == null)
         {
            graphics = getGraphics();
            if (graphics == null)
            {
               return;
            }
            image         = createImage(canvasSize.width, canvasSize.height);
            imageGraphics = image.getGraphics();
            graphics.setFont(font);
            imageGraphics.setFont(font);
            fontMetrics = graphics.getFontMetrics();
            fontAscent  = fontMetrics.getMaxAscent();
            fontWidth   = fontMetrics.getMaxAdvance();
            fontHeight  = fontMetrics.getHeight();
         }

         // Clear display.
         imageGraphics.setColor(Color.white);
         imageGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);

         // Draw cells.
         int n = Nest.MAX_ELEVATION + 1;
         Color[] colors = new Color[n];
         for (int i = 0; i < n; i++)
         {
            float s = (float)(n - (i + 1)) / (float)(n - 1);
            int   r = 255 - (int)(255.0f * s);
            int   g = 255 - (int)(255.0f * s);
            int   b = 255 - (int)(255.0f * s);
            colors[i] = new Color(r, g, b);
         }
         for (x = x2 = 0; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = canvasSize.height - (int)cellHeight;
                 y < height;
                 y++, y2 = (int)(cellHeight * (double)(height - (y + 1))))
            {
               imageGraphics.setColor(colors[nest.cells[x][y][Nest.ELEVATION_CELL_INDEX]]);
               imageGraphics.fillRect(x2, y2, (int)cellWidth + 1, (int)cellHeight + 1);
            }
         }

         // Draw grid.
         imageGraphics.setColor(Color.black);
         y2 = canvasSize.height;
         for (x = 1, x2 = (int)cellWidth; x < width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            imageGraphics.drawLine(x2, 0, x2, y2);
         }
         x2 = canvasSize.width;
         for (y = 1, y2 = (int)cellHeight; y < height;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            imageGraphics.drawLine(0, y2, x2, y2);
         }

         // Draw fish level.
         imageGraphics.setColor(Color.yellow);
         String s = "Fish elevation=" + fishElevation;
         if (fishInfoMsg)
         {
            s += " (Scroll to change elevation. Plow with mouse.)";
         }
         imageGraphics.drawString(s, 2, fontHeight);

         // Refresh display.
         graphics.drawImage(image, 0, 0, this);
      }


      // Canvas mouse listener.
      class CanvasMouseListener extends MouseAdapter
      {
         // Mouse pressed.
         public void mousePressed(MouseEvent evt)
         {
            fishInfoMsg = false;

            int x = (int)((double)evt.getX() / cellWidth);
            int y = height - (int)((double)evt.getY() / cellHeight) - 1;

            if ((x >= 0) && (x < width) &&
                (y >= 0) && (y < height))
            {
               selectedX = x;
               selectedY = y;
            }
         }


         // Mouse released.
         public void mouseReleased(MouseEvent evt)
         {
            fishInfoMsg = false;
            selectedX   = selectedY = -1;
         }
      }

      // Canvas mouse motion listener.
      class CanvasMouseMotionListener extends MouseMotionAdapter
      {
         // Mouse dragged.
         public void mouseDragged(MouseEvent evt)
         {
            fishInfoMsg = false;

            int x = (int)((double)evt.getX() / cellWidth);
            int y = height - (int)((double)evt.getY() / cellHeight) - 1;

            if ((x >= 0) && (x < width) &&
                (y >= 0) && (y < height))
            {
               if ((selectedX != -1) && (fishElevation < nest.cells[selectedX][selectedY][0]))
               {
                  if ((selectedX != x) || (selectedY != y))
                  {
                     int[] plowX = new int[3];
                     int[] plowY = new int[3];
                     if (x < selectedX)
                     {
                        for (int i = 0; i < 3; i++)
                        {
                           plowX[i] = x;
                        }
                        plowY[0] = (y + 1) % height;
                        plowY[1] = y;
                        plowY[2] = y - 1;
                        if (plowY[2] < 0)
                        {
                           plowY[2] += height;
                        }
                     }
                     else if (x > selectedX)
                     {
                        for (int i = 0; i < 3; i++)
                        {
                           plowX[i] = x;
                        }
                        plowY[0] = (y + 1) % height;
                        plowY[1] = y;
                        plowY[2] = y - 1;
                        if (plowY[2] < 0)
                        {
                           plowY[2] += height;
                        }
                     }
                     else if (y < selectedY)
                     {
                        for (int i = 0; i < 3; i++)
                        {
                           plowY[i] = y;
                        }
                        plowX[0] = (x + 1) % width;
                        plowX[1] = x;
                        plowX[2] = x - 1;
                        if (plowX[2] < 0)
                        {
                           plowX[2] += width;
                        }
                     }
                     else if (y > selectedY)
                     {
                        for (int i = 0; i < 3; i++)
                        {
                           plowY[i] = y;
                        }
                        plowX[0] = (x + 1) % width;
                        plowX[1] = x;
                        plowX[2] = x - 1;
                        if (plowX[2] < 0)
                        {
                           plowX[2] += width;
                        }
                     }
                     int n = nest.cells[selectedX][selectedY][0] - fishElevation;
                     nest.cells[selectedX][selectedY][0] = fishElevation;
                     nest.cells[plowX[1]][plowY[1]][0]  += n;
                     if (nest.cells[plowX[1]][plowY[1]][0] > Nest.MAX_ELEVATION)
                     {
                        nest.cells[plowX[1]][plowY[1]][0] = Nest.MAX_ELEVATION;
                     }
                     //int j = random.nextInt(3);
                     //for (int i = 0; i < n; i++)
                     //{
                     //nest.cells[plowX[j]][plowY[j]][0]++;
                     //if (nest.cells[plowX[j]][plowY[j]][0] > Nest.MAX_ELEVATION)
                     //{
                     //nest.cells[plowX[j]][plowY[j]][0] = Nest.MAX_ELEVATION;
                     //}
                     //j = (j + 1) % 3;
                     //}
                  }
               }
               selectedX = x;
               selectedY = y;
            }
         }
      }

      // Mouse wheel moved.
      public void mouseWheelMoved(MouseWheelEvent e)
      {
         fishInfoMsg    = false;
         fishElevation += e.getWheelRotation();
         if (fishElevation < 0)
         {
            fishElevation = 0;
         }
         else if (fishElevation > Nest.MAX_ELEVATION)
         {
            fishElevation = Nest.MAX_ELEVATION;
         }
      }
   }

   // Main.
   public static void main(String[] args)
   {
      // Get options.
      int    width        = -1;
      int    height       = -1;
      int    maxElevation = -1;
      int    randomSeed   = Main.DEFAULT_RANDOM_SEED;
      String loadfile     = null;
      String savefile     = null;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-nestDimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid nestDmensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               width = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid nest width");
               System.err.println(Usage);
               System.exit(1);
            }
            if (width < 2)
            {
               System.err.println("Invalid nest width");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid nestDimensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               height = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid nest height");
               System.err.println(Usage);
               System.exit(1);
            }
            if (height < 2)
            {
               System.err.println("Invalid nest height");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-maxElevation"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Nest.MAX_ELEVATION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.MAX_ELEVATION < 0)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-load"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid load option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (loadfile == null)
            {
               loadfile = args[i];
            }
            else
            {
               System.err.println("Duplicate load option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-save"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid save option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (savefile == null)
            {
               savefile = args[i];
            }
            else
            {
               System.err.println("Duplicate save option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         System.err.println(Usage);
         System.exit(1);
      }

      // Check options.
      if (loadfile == null)
      {
         if ((width == -1) || (height == -1))
         {
            System.err.println(Usage);
            System.exit(1);
         }
      }
      else
      {
         if ((maxElevation != -1) || (width != -1) || (height != -1))
         {
            System.err.println(Usage);
            System.exit(1);
         }
      }

      // Set look and feel.
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         System.err.println("Warning: cannot set look and feel");
      }

      // Create nest.
      Nest nest;
      if (loadfile == null)
      {
         nest = new Nest(new Dimension(width, height), randomSeed);
      }
      else
      {
         nest = new Nest();
         try
         {
            nest.load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(1);
         }
      }

      // Create editor.
      NestEditor editor = new NestEditor(nest);

      // Run editor.
      editor.run();

      // Save?
      if (savefile != null)
      {
         try
         {
            nest.save(savefile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save to file " + savefile + ": " + e.getMessage());
            System.exit(1);
         }
      }
      System.exit(0);
   }
}
