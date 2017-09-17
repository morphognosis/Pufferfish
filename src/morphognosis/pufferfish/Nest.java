// For conditions of distribution and use, see copyright notice in Main.java

// Nest.

package morphognosis.pufferfish;

import java.io.*;
import javax.imageio.ImageIO;

import morphognosis.SectorDisplay;
import morphognosis.Utility;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Nest
{
   // Cells.
   // See SectorDisplay.EMPTY_CELL_VALUE.
   public static final int CELL_DIMENSIONS      = 1;
   public static final int ELEVATION_CELL_INDEX = 0;
   public static int       MAX_ELEVATION_VALUE  = 99;
   public Dimension        size;
   public int[][][]        cells;
   public int[][][]        restoreCells;

   // Nest image file.
   String nestImageFile;

   // Constructors.
   public Nest(Dimension size, String nestImageFile)
   {
      int x, y, width, height;

      this.nestImageFile = nestImageFile;

      // Create cells.
      this.size    = size;
      width        = size.width;
      height       = size.height;
      cells        = new int[width][height][CELL_DIMENSIONS];
      restoreCells = new int[width][height][CELL_DIMENSIONS];
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d]        = SectorDisplay.EMPTY_CELL_VALUE;
               restoreCells[x][y][d] = SectorDisplay.EMPTY_CELL_VALUE;
            }
         }
      }

      // Load elevations from nest image.
      loadNestImageElevations();
      checkpoint();
   }


   public Nest()
   {
      size = new Dimension();
   }


   // Get grid width.
   public int getWidth()
   {
      return(size.width);
   }


   // Get grid height.
   public int getHeight()
   {
      return(size.height);
   }


   // Cell distance.
   public int cellDist(int fromX, int fromY, int toX, int toY)
   {
      int w  = size.width;
      int w2 = w / 2;
      int h  = size.height;
      int h2 = h / 2;
      int dx = Math.abs(toX - fromX);

      if (dx > w2) { dx = w - dx; }
      int dy = Math.abs(toY - fromY);
      if (dy > h2) { dy = h - dy; }
      return(dx + dy);
   }


   // Load nest image elevations.
   private void loadNestImageElevations()
   {
      // Load image as resource.
      Image image = null;

      try
      {
         image = (Image)ImageIO.read(getClass().getResource(nestImageFile));
      }
      catch (Exception e)
      {
      }

      // Load external image file.
      if (image == null)
      {
         try
         {
            image = (Image)ImageIO.read(new File(nestImageFile));
         }
         catch (Exception e)
         {
         }
      }

      if (image == null)
      {
         System.err.println("Cannot load nest image file " + nestImageFile);
         return;
      }

      // Create cells image.
      int           w            = size.width;
      int           h            = size.height;
      int           numCellTypes = MAX_ELEVATION_VALUE + 1;
      float         q            = 256.0f / (float)numCellTypes;
      Image         s            = image.getScaledInstance(w, h, Image.SCALE_DEFAULT);
      BufferedImage b            = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      Graphics      g            = b.createGraphics();
      g.drawImage(s, 0, 0, null);
      g.dispose();
      for (int x = 0; x < w; x++)
      {
         for (int y = 0; y < h; y++)
         {
            int cy = (h - 1) - y;
            int t  = (int)((float)(b.getRGB(x, y) & 0xFF) / q);
            if (t >= numCellTypes)
            {
               t = numCellTypes - 1;
            }
            cells[x][cy][ELEVATION_CELL_INDEX] = t;
         }
      }
   }


   // Save cells.
   public void save(String filename) throws IOException
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(output);
      output.close();
   }


   // Save cells.
   public void save(FileOutputStream output) throws IOException
   {
      int x, y;

      PrintWriter writer = new PrintWriter(output);

      Utility.saveInt(writer, size.width);
      Utility.saveInt(writer, size.height);
      Utility.saveInt(writer, MAX_ELEVATION_VALUE);

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, cells[x][y][d]);
            }
         }
      }

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, restoreCells[x][y][d]);
            }
         }
      }
   }


   // Load cells from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load cells.
   public void load(FileInputStream input) throws IOException
   {
      int w, h, x, y;

      DataInputStream reader = new DataInputStream(input);

      w = Utility.loadInt(reader);
      h = Utility.loadInt(reader);
      MAX_ELEVATION_VALUE = Utility.loadInt(reader);

      size.width   = w;
      size.height  = h;
      cells        = new int[size.width][size.height][2];
      restoreCells = new int[size.width][size.height][2];
      clear();

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }
   }


   // Clear cells.
   public void clear()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = SectorDisplay.EMPTY_CELL_VALUE;
            }
         }
      }
   }


   // Checkpoint cells.
   public void checkpoint()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = cells[x][y][d];
            }
         }
      }
   }


   // Restore cells.
   public void restore()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = restoreCells[x][y][d];
            }
         }
      }
   }
}
