/*
 * Copyright (c) 2017 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Main.

package morphognosis.pufferfish;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.swing.UIManager;

import morphognosis.Morphognostic;
import morphognosis.Orientation;

public class Main
{
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java morphognosis.pufferfish.Main\n" +
      "      -steps <steps> | -display\n" +
      "      -dimensions <width> <height>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (pufferfish driver: default=autopilot)]\n" +
      "     [-maxElevation <quantity> (default=" + Nest.MAX_ELEVATION_VALUE + ")]\n" +
      "     [-numNeighborhoods <quantity> (default=" + Morphognostic.DEFAULT_NUM_NEIGHBORHOODS + ")]\n" +
      "     [-neighborhoodInitialDimension <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "     [-neighborhoodDimensionStride <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "     [-neighborhoodDimensionMultiplier <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "     [-epochIntervalStride <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE + ")]\n" +
      "     [-epochIntervalMultiplier <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "  Resume run:\n" +
      "    java morphognosis.pufferfish.Main\n" +
      "      -steps <steps> | -display\n" +
      "      -load <file name>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (default=autopilot)]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=error";

   // Nest image.
   public static final String NEST_IMAGE_FILE = "pufferfish_nest.png";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Pufferfish.
   public Pufferfish pufferfish;

   // Nest.
   public Nest nest;

   // Display.
   public NestDisplay display;

   // Random numbers.
   int          randomSeed;
   SecureRandom random;

   // Constructor.
   public Main(int randomSeed)
   {
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
   }


   // Initialize.
   public void init(int width, int height,
                    int NUM_NEIGHBORHOODS,
                    int NEIGHBORHOOD_INITIAL_DIMENSION,
                    int NEIGHBORHOOD_DIMENSION_STRIDE,
                    int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                    int EPOCH_INTERVAL_STRIDE,
                    int EPOCH_INTERVAL_MULTIPLIER)
   {
      // Create nest.
      nest = new Nest(new Dimension(width, height), NEST_IMAGE_FILE);

      // Create pufferfish.
      pufferfish = new Pufferfish(0, nest.nestX, nest.nestY,
                                  Orientation.NORTH, nest, randomSeed,
                                  NUM_NEIGHBORHOODS,
                                  NEIGHBORHOOD_INITIAL_DIMENSION,
                                  NEIGHBORHOOD_DIMENSION_STRIDE,
                                  NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                  EPOCH_INTERVAL_STRIDE,
                                  EPOCH_INTERVAL_MULTIPLIER);
   }


   // Get width.
   int getWidth()
   {
      if (nest != null)
      {
         return(nest.size.width);
      }
      else
      {
         return(0);
      }
   }


   // Get height.
   int getHeight()
   {
      if (nest != null)
      {
         return(nest.size.height);
      }
      else
      {
         return(0);
      }
   }


   // Reset.
   public void reset()
   {
      random.setSeed(randomSeed);
      if (nest != null)
      {
         nest.restore();
      }
      if (pufferfish != null)
      {
         pufferfish.reset();
      }
      if (display != null)
      {
         display.close();
      }
   }


   // Clear.
   public void clear()
   {
      if (display != null)
      {
         display.close();
         display = null;
      }
      nest       = null;
      pufferfish = null;
   }


   // Save to file.
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


   // Save.
   public void save(FileOutputStream output) throws IOException
   {
      // Save cells.
      nest.save(output);

      // Save pufferfish.
      pufferfish.save(output);
   }


   // Load from file.
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


   // Load.
   public void load(FileInputStream input) throws IOException
   {
      // Load cells.
      nest = new Nest();
      nest.load(input);

      // Load pufferfish.
      pufferfish = new Pufferfish(nest, randomSeed);
      pufferfish.load(input);
   }


   // Run.
   public void run(int steps)
   {
      random.setSeed(randomSeed);
      if (steps >= 0)
      {
         stepPufferfish();
      }
      else
      {
         for (int i = 0; updateDisplay(i); i++)
         {
            stepPufferfish();
         }
      }
   }


   // Step pufferfish.
   void stepPufferfish()
   {
      int x, y, cx, cy, ux, uy, dx, dy, lx, ly, rx, ry, width, height;
      int response;

      float[] sensors = new float[Pufferfish.NUM_SENSORS];

      width  = nest.size.width;
      height = nest.size.height;

      // Initialize sensors.
      cx = pufferfish.x;
      cy = pufferfish.y;
      ux = dx = lx = rx = cx;
      uy = dy = ly = ry = cy;
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
            break;

         case 1:
            y  = ((y + 1) % height);
            ux = x;
            uy = y;
            break;

         case 2:
            x = ((x + 1) % width);
            y = ((y + 1) % height);
            break;

         case 3:
            x--;
            if (x < 0) { x += width; }
            lx = x;
            ly = y;
            break;

         case 4:
            break;

         case 5:
            x  = ((x + 1) % width);
            rx = x;
            ry = y;
            break;

         case 6:
            x--;
            if (x < 0) { x += width; }
            y--;
            if (y < 0) { y += height; }
            break;

         case 7:
            y--;
            if (y < 0) { y += height; }
            dx = x;
            dy = y;
            break;

         case 8:
            x = ((x + 1) % width);
            y--;
            if (y < 0) { y += height; }
            break;
         }
         sensors[i] = (float)nest.cells[x][y][Nest.ELEVATION_CELL_INDEX];
      }

      // Update landmarks.
      pufferfish.landmarkMap[pufferfish.x][pufferfish.y] = true;

      // Cycle pufferfish.
      response = pufferfish.cycle(sensors);

      // Process response.
      switch (response)
      {
      case Pufferfish.UP:
         pufferfish.x = ux;
         pufferfish.y = uy;
         break;

      case Pufferfish.DOWN:
         pufferfish.x = dx;
         pufferfish.y = dy;
         break;

      case Pufferfish.LEFT:
         pufferfish.x = lx;
         pufferfish.y = ly;
         break;

      case Pufferfish.RIGHT:
         pufferfish.x = rx;
         pufferfish.y = ry;
         break;

      case Pufferfish.RAISE:
         if (nest.cells[cx][cy][Nest.ELEVATION_CELL_INDEX] < Nest.MAX_ELEVATION_VALUE)
         {
            nest.cells[cx][cy][Nest.ELEVATION_CELL_INDEX]++;
         }
         break;

      case Pufferfish.LOWER:
         if (nest.cells[cx][cy][Nest.ELEVATION_CELL_INDEX] > 0)
         {
            nest.cells[cx][cy][Nest.ELEVATION_CELL_INDEX]--;
         }
         break;
      }
   }


   // Create display.
   public void createDisplay()
   {
      if (display == null)
      {
         display = new NestDisplay(nest, pufferfish);
      }
   }


   // Destroy display.
   public void destroyDisplay()
   {
      if (display != null)
      {
         display.close();
         display = null;
      }
   }


   // Update display.
   // Return false for display quit.
   public boolean updateDisplay(int steps)
   {
      if (display != null)
      {
         display.update(steps);
         if (display.quit)
         {
            display = null;
            return(false);
         }
         else
         {
            return(true);
         }
      }
      else
      {
         return(false);
      }
   }


   // Main.
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      // Get options.
      int     steps             = -1;
      int     width             = -1;
      int     height            = -1;
      int     driver            = Pufferfish.DRIVER_TYPE.AUTOPILOT.getValue();
      int     maxElevation      = -1;
      int     randomSeed        = DEFAULT_RANDOM_SEED;
      String  loadfile          = null;
      String  savefile          = null;
      boolean display           = false;
      boolean gotParm           = false;
      int     NUM_NEIGHBORHOODS = Morphognostic.DEFAULT_NUM_NEIGHBORHOODS;
      int     NEIGHBORHOOD_INITIAL_DIMENSION    = Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
      int     NEIGHBORHOOD_DIMENSION_STRIDE     = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
      int     NEIGHBORHOOD_DIMENSION_MULTIPLIER = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      int     EPOCH_INTERVAL_STRIDE             = Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE;
      int     EPOCH_INTERVAL_MULTIPLIER         = Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               steps = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (steps < 0)
            {
               System.err.println("Invalid steps option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-display"))
         {
            display = true;
            continue;
         }
         if (args[i].equals("-dimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               width = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid width option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (width < 2)
            {
               System.err.println("Invalid width option");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               height = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid height option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (height < 2)
            {
               System.err.println("Invalid height option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-driver"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid driver option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (args[i].equals("metamorphDB"))
            {
               driver = Pufferfish.DRIVER_TYPE.METAMORPH_DB.getValue();
            }
            else if (args[i].equals("metamorphNN"))
            {
               driver = Pufferfish.DRIVER_TYPE.METAMORPH_NN.getValue();
            }
            else if (args[i].equals("autopilot"))
            {
               driver = Pufferfish.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            else
            {
               System.err.println("Invalid driver option");
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
               Nest.MAX_ELEVATION_VALUE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Nest.MAX_ELEVATION_VALUE < 0)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numNeighborhoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_NEIGHBORHOODS < 0)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodInitialDimension"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NEIGHBORHOOD_DIMENSION_STRIDE < 0)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCH_INTERVAL_STRIDE < 0)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCH_INTERVAL_MULTIPLIER < 0)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
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
      if (((steps < 0) && !display) || ((steps >= 0) && display))
      {
         System.err.println(Usage);
         System.exit(1);
      }
      if (!display)
      {
         if (driver == Pufferfish.DRIVER_TYPE.MANUAL.getValue())
         {
            System.err.println("Cannot run manually without display");
            System.err.println(Usage);
            System.exit(1);
         }
      }
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
         if ((maxElevation != -1) || (width != -1) || (height != -1) || gotParm)
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

      // Create world.
      Main main = new Main(randomSeed);
      if (loadfile != null)
      {
         try
         {
            main.load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(1);
         }
      }
      else
      {
         try
         {
            main.init(width, height,
                      NUM_NEIGHBORHOODS,
                      NEIGHBORHOOD_INITIAL_DIMENSION,
                      NEIGHBORHOOD_DIMENSION_STRIDE,
                      NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                      EPOCH_INTERVAL_STRIDE,
                      EPOCH_INTERVAL_MULTIPLIER);
         }
         catch (Exception e)
         {
            System.err.println("Cannot initialize: " + e.getMessage());
            System.exit(1);
         }
      }

      // Create display?
      if (display)
      {
         main.createDisplay();
      }
      else
      {
         main.reset();
      }

      // Set pufferfish driver.
      main.pufferfish.driver = driver;
      if (driver == Pufferfish.DRIVER_TYPE.METAMORPH_NN.getValue())
      {
         try
         {
            System.out.println("Training metamorph NN...");
            main.pufferfish.createMetamorphNN();
         }
         catch (Exception e)
         {
            System.err.println("Cannot train metamorph NN: " + e.getMessage());
         }
      }

      // Run.
      main.run(steps);

      // Save?
      if (savefile != null)
      {
         try
         {
            main.save(savefile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save to file " + savefile + ": " + e.getMessage());
         }
      }
      System.exit(0);
   }
}
