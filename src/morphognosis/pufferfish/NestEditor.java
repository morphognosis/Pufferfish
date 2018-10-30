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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import morphognosis.Orientation;

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

   // Pufferfish.
   Pufferfish pufferfish;

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
      // Save nest.
      this.nest = nest;

      // Create pufferfish.
      pufferfish = new Pufferfish(nest, randomSeed);

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
      requestFocusInWindow();
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
   public class NestDisplay extends Canvas implements KeyListener, MouseWheelListener
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

      // Lock.
      private Object lock;

      // Constructor.
      public NestDisplay(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());
         addKeyListener(this);
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

         // Get synchronization lock.
         lock = new Object();
      }


      // Update display.
      void update()
      {
         int x, y, x2, y2;

         synchronized (lock)
         {
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
               requestFocusInWindow();
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

            // Draw pufferfish.
            int[] vx = new int[3];
            int[] vy = new int[3];
            x2       = (int)(cellWidth * (double)pufferfish.x);
            y2       = (int)(cellHeight * (double)(height - (pufferfish.y + 1)));
            if (pufferfish.orientation == Orientation.NORTH)
            {
               vx[0] = x2 + (int)(cellWidth * 0.5f);
               vy[0] = y2;
               vx[1] = x2;
               vy[1] = y2 + (int)cellHeight;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2 + (int)cellHeight;
            }
            else if (pufferfish.orientation == Orientation.EAST)
            {
               vx[0] = x2 + (int)(cellWidth);
               vy[0] = y2 + (int)(cellHeight * 0.5f);
               vx[1] = x2;
               vy[1] = y2;
               vx[2] = x2;
               vy[2] = y2 + (int)cellHeight;
            }
            else if (pufferfish.orientation == Orientation.SOUTH)
            {
               vx[0] = x2 + (int)(cellWidth * 0.5f);
               vy[0] = y2 + (int)cellHeight;
               vx[1] = x2;
               vy[1] = y2;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2;
            }
            else
            {
               vx[0] = x2;
               vy[0] = y2 + (int)(cellHeight * 0.5f);
               vx[1] = x2 + (int)cellWidth;
               vy[1] = y2;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2 + (int)cellHeight;
            }
            imageGraphics.setColor(Color.red);
            imageGraphics.fillPolygon(vx, vy, 3);

            // Draw fish level.
            imageGraphics.setColor(Color.yellow);
            String s = "Fish elevation=" + fishElevation;
            if (fishInfoMsg)
            {
               s += " (Scroll to change elevation. Plow with arrows or mouse.)";
            }
            imageGraphics.drawString(s, 2, fontHeight);

            // Refresh display.
            graphics.drawImage(image, 0, 0, this);
         }
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
               if (selectedX != -1)
               {
                  plow(selectedX, selectedY, x, y);
               }
               selectedX = x;
               selectedY = y;
            }
         }
      }

      // Key event handlers.
      public void keyTyped(KeyEvent e)
      {
         fishInfoMsg = false;
      }


      public void keyPressed(KeyEvent e)
      {
         int x, y;

         fishInfoMsg = false;
         if (e.getID() != KeyEvent.KEY_TYPED)
         {
            switch (e.getKeyCode())
            {
            // Up arrow=forward.
            case 38:
               switch (pufferfish.orientation)
               {
               case Orientation.NORTH:
                  y = (pufferfish.y + 1) % height;
                  plow(pufferfish.x, pufferfish.y, pufferfish.x, y);
                  pufferfish.y = y;
                  break;

               case Orientation.EAST:
                  x = (pufferfish.x + 1) % width;
                  plow(pufferfish.x, pufferfish.y, x, pufferfish.y);
                  pufferfish.x = x;
                  break;

               case Orientation.SOUTH:
                  y = pufferfish.y - 1;
                  if (y < 0)
                  {
                     y += height;
                  }
                  plow(pufferfish.x, pufferfish.y, pufferfish.x, y);
                  pufferfish.y = y;
                  break;

               case Orientation.WEST:
                  x = pufferfish.x - 1;
                  if (x < 0)
                  {
                     x += width;
                  }
                  plow(pufferfish.x, pufferfish.y, x, pufferfish.y);
                  pufferfish.x = x;
                  break;
               }
               break;

            // Left arrow=turn left.
            case 37:
               pufferfish.orientation--;
               if (pufferfish.orientation < 0)
               {
                  pufferfish.orientation += Orientation.NUM_ORIENTATIONS;
               }
               break;

            // Right arrow=turn right.
            case 39:
               pufferfish.orientation =
                  (pufferfish.orientation + 1) % Orientation.NUM_ORIENTATIONS;
               break;
            }
         }
      }


      public void keyReleased(KeyEvent e)
      {
         fishInfoMsg = false;
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


      // Plow the surface.
      void plow(int fromX, int fromY, int toX, int toY)
      {
         synchronized (lock)
         {
            if (fishElevation < nest.cells[toX][toY][0])
            {
               if ((fromX != toX) || (fromY != toY))
               {
                  int[] plowX = new int[3];
                  int[] plowY = new int[3];
                  if ((toX < fromX) || ((toX == (width - 1)) && (fromX == 0)))
                  {
                     plowX[0] = toX;
                     plowX[1] = toX - 1;
                     if (plowX[1] < 0)
                     {
                        plowX[1] += width;
                     }
                     plowX[2] = toX;
                     plowY[0] = (toY + 1) % height;
                     plowY[1] = toY;
                     plowY[2] = toY - 1;
                     if (plowY[2] < 0)
                     {
                        plowY[2] += height;
                     }
                  }
                  else if ((toX > fromX) || ((toX == 0) && (fromX == (width - 1))))
                  {
                     plowX[0] = toX;
                     plowX[1] = (toX + 1) % width;
                     plowX[2] = toX;
                     plowY[0] = (toY + 1) % height;
                     plowY[1] = toY;
                     plowY[2] = toY - 1;
                     if (plowY[2] < 0)
                     {
                        plowY[2] += height;
                     }
                  }
                  else if ((toY < fromY) || ((toY == (height - 1)) && (fromY == 0)))
                  {
                     plowY[0] = toY;
                     plowY[1] = toY - 1;
                     if (plowY[1] < 0)
                     {
                        plowY[1] += height;
                     }
                     plowY[2] = toY;
                     plowX[0] = (toX + 1) % width;
                     plowX[1] = toX;
                     plowX[2] = toX - 1;
                     if (plowX[2] < 0)
                     {
                        plowX[2] += width;
                     }
                  }
                  else if ((toY > fromY) || ((toY == 0) && (fromY == (height - 1))))
                  {
                     plowY[0] = toY;
                     plowY[1] = (toY + 1) % height;
                     plowY[2] = toY;
                     plowX[0] = (toX + 1) % width;
                     plowX[1] = toX;
                     plowX[2] = toX - 1;
                     if (plowX[2] < 0)
                     {
                        plowX[2] += width;
                     }
                  }
                  int n = nest.cells[toX][toY][Nest.ELEVATION_CELL_INDEX] - fishElevation;
                  nest.cells[toX][toY][0] = fishElevation;
                  int j = random.nextInt(3);
                  for (int i = 0; i < n; i++)
                  {
                     nest.cells[plowX[j]][plowY[j]][0]++;
                     if (nest.cells[plowX[j]][plowY[j]][0] > Nest.MAX_ELEVATION)
                     {
                        nest.cells[plowX[j]][plowY[j]][0] = Nest.MAX_ELEVATION;
                     }
                     j = (j + 1) % 3;
                  }
               }
            }
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
