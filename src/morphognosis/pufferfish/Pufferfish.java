// For conditions of distribution and use, see copyright notice in Main.java

// Pufferfish: morphognosis organism.

package morphognosis.pufferfish;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Vector;
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Morphognostic.Neighborhood;
import morphognosis.Orientation;
import morphognosis.Utility;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;

public class Pufferfish
{
   // Properties.
   public int          x, y;
   public int          orientation;
   public Nest         nest;
   public int          x2, y2;
   public int          orientation2;
   public int          driver;
   public int          driverResponse;
   public int          randomSeed;
   public SecureRandom random;

   // Maximum distance between equivalent morphognostics.
   public static float EQUIVALENT_MORPHOGNOSTIC_DISTANCE = 0.0f;

   // Current morphognostic.
   public Morphognostic morphognostic;

   // Metamorphs.
   public ArrayList<Metamorph> metamorphs;
   public FastVector           metamorphNNattributeNames;
   public Instances            metamorphInstances;
   MultilayerPerceptron        metamorphNN;
   public static final boolean saveMetamorphInstances = false;
   public static final boolean saveMetamorphNN        = false;
   public static final boolean evaluateMetamorphNN    = true;

   // Input/output.
   public static final int NUM_SENSORS = 9;
   float[] sensors;
   int response;

   // Response types.
   public static final int WAIT          = 0;
   public static final int FORWARD       = 1;
   public static final int TURN_LEFT     = 2;
   public static final int TURN_RIGHT    = 3;
   public static final int RAISE         = 4;
   public static final int LOWER         = 5;
   public static final int NUM_RESPONSES = 6;

   // Navigation.
   public boolean[][] landmarkMap;
   public int         maxEventAge;
   public class Event
   {
      public int[] values;
      public int   x;
      public int   y;
      public int   time;
      public Event(int[] values, int x, int y, int time)
      {
         int n = values.length;

         this.values = new int[n];
         for (int i = 0; i < n; i++)
         {
            this.values[i] = values[i];
         }
         this.x    = x;
         this.y    = y;
         this.time = time;
      }
   }
   public Vector<Event> events;
   public int           eventTime;

   // Driver type.
   public enum DRIVER_TYPE
   {
      METAMORPH_DB(0),
      METAMORPH_NN(1),
      AUTOPILOT(2),
      MANUAL(3);

      private int value;

      DRIVER_TYPE(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return(value);
      }
   }

   // Autopilot response.
   int radius;
   int ring;
   int step;

   // Constructors.
   public Pufferfish(Nest nest, int randomSeed)
   {
      this.nest       = nest;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init();
      int [] numEventTypes = new int[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         numEventTypes[i] = Nest.MAX_ELEVATION + 1;
      }
      morphognostic = new Morphognostic(Orientation.NORTH, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   public Pufferfish(Nest nest, int randomSeed,
                     int NUM_NEIGHBORHOODS,
                     int NEIGHBORHOOD_INITIAL_DIMENSION,
                     int NEIGHBORHOOD_DIMENSION_STRIDE,
                     int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                     int EPOCH_INTERVAL_STRIDE,
                     int EPOCH_INTERVAL_MULTIPLIER)
   {
      this.nest       = nest;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init();
      int [] numEventTypes = new int[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         numEventTypes[i] = Nest.MAX_ELEVATION + 1;
      }
      morphognostic = new Morphognostic(Orientation.NORTH, numEventTypes,
                                        NUM_NEIGHBORHOODS,
                                        NEIGHBORHOOD_INITIAL_DIMENSION,
                                        NEIGHBORHOOD_DIMENSION_STRIDE,
                                        NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                        EPOCH_INTERVAL_STRIDE,
                                        EPOCH_INTERVAL_MULTIPLIER);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   // Initialize.
   void init()
   {
      x           = x2 = nest.size.width / 2;
      y           = y2 = nest.size.height / 2;
      orientation = orientation2 = Orientation.NORTH;
      sensors     = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response       = WAIT;
      driver         = DRIVER_TYPE.METAMORPH_DB.getValue();
      driverResponse = WAIT;
      landmarkMap    = new boolean[nest.size.width][nest.size.height];
      for (int i = 0; i < nest.size.width; i++)
      {
         for (int j = 0; j < nest.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events    = new Vector<Event>();
      eventTime = 0;
      initAutopilot();
   }


   // Reset state.
   void reset()
   {
      random.setSeed(randomSeed);
      x           = x2;
      y           = y2;
      orientation = orientation2;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverResponse = WAIT;
      for (int i = 0; i < nest.size.width; i++)
      {
         for (int j = 0; j < nest.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events.clear();
      morphognostic.clear();
      initAutopilot();
   }


   // Save pufferfish to file.
   public void save(String filename) throws IOException
   {
      DataOutputStream writer;

      try
      {
         writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(writer);
      writer.close();
   }


   // Save pufferfish.
   public void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, orientation);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, orientation2);
      morphognostic.save(writer);
      Utility.saveInt(writer, maxEventAge);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(writer);
      }
      Utility.saveFloat(writer, EQUIVALENT_MORPHOGNOSTIC_DISTANCE);
      writer.flush();
   }


   // Load pufferfish from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try
      {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load pufferfish.
   public void load(FileInputStream input) throws IOException
   {
      // Load the properties.
      // DataInputStream is for unbuffered input.
      DataInputStream reader = new DataInputStream(input);

      x             = Utility.loadInt(reader);
      y             = Utility.loadInt(reader);
      orientation   = Utility.loadInt(reader);
      x2            = Utility.loadInt(reader);
      y2            = Utility.loadInt(reader);
      orientation2  = Utility.loadInt(reader);
      morphognostic = Morphognostic.load(reader);
      maxEventAge   = Utility.loadInt(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Utility.loadFloat(reader);
      initMetamorphNN();
      initAutopilot();
   }


   // Sensor/response cycle.
   public int cycle(float[] sensors)
   {
      // Update morphognostic.
      int[] values = new int[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         this.sensors[i] = sensors[i];
         values[i]       = (int)sensors[i];
      }
      events.add(new Event(values, x, y, eventTime));
      if ((eventTime - events.get(0).time) > maxEventAge)
      {
         events.remove(0);
      }
      int w = nest.size.width;
      int h = nest.size.height;
      int a = maxEventAge + 1;
      int morphEvents[][][][] = new int[w][h][NUM_SENSORS][a];
      for (int x2 = 0; x2 < w; x2++)
      {
         for (int y2 = 0; y2 < h; y2++)
         {
            for (int n = 0; n < NUM_SENSORS; n++)
            {
               for (int t = 0; t < a; t++)
               {
                  morphEvents[x2][y2][n][t] = -1;
               }
            }
         }
      }
      for (Event e : events)
      {
         for (int n = 0; n < NUM_SENSORS; n++)
         {
            morphEvents[e.x][e.y][n][eventTime - e.time] = e.values[n];
         }
      }
      morphognostic.update(morphEvents, x, y);

      // Respond.
      if (driver == DRIVER_TYPE.METAMORPH_DB.getValue())
      {
         metamorphDBresponse();
      }
      else if (driver == DRIVER_TYPE.METAMORPH_NN.getValue())
      {
         metamorphNNresponse();
      }
      else if (driver == DRIVER_TYPE.AUTOPILOT.getValue())
      {
         autoPilotResponse();
      }
      else
      {
         response = driverResponse;
      }

      // Update metamorphs.
      Metamorph metamorph = new Metamorph(morphognostic.clone(), response);
      boolean   found     = false;

      for (Metamorph m : metamorphs)
      {
         for (int i = 0; i < Orientation.NUM_ORIENTATIONS; i++)
         {
            metamorph.morphognostic.orientation = i;
            if (m.morphognostic.compare(metamorph.morphognostic) <=
                EQUIVALENT_MORPHOGNOSTIC_DISTANCE)
            {
               found = true;
               break;
            }
         }
         if (found) { break; }
      }
      metamorph.morphognostic.orientation = Orientation.NORTH;
      if (!found)
      {
         metamorphs.add(metamorph);
      }

      eventTime++;
      return(response);
   }


   // Get metamorph DB response.
   void metamorphDBresponse()
   {
      response = WAIT;
      Metamorph metamorph = null;
      float     d         = 0.0f;
      float     d2;
      for (Metamorph m : metamorphs)
      {
         for (int i = 0; i < Orientation.NUM_ORIENTATIONS; i++)
         {
            morphognostic.orientation = i;
            d2 = morphognostic.compare(m.morphognostic);
            if ((metamorph == null) || (d2 < d))
            {
               d         = d2;
               metamorph = m;
            }
            else
            {
               if (d2 == d)
               {
                  if (random.nextBoolean())
                  {
                     d         = d2;
                     metamorph = m;
                  }
               }
            }
         }
      }
      morphognostic.orientation = Orientation.NORTH;
      if (metamorph != null)
      {
         response = metamorph.response;
      }
   }


   // Get metamorph neural network response.
   void metamorphNNresponse()
   {
      response = WAIT;
      double d = -1.0;
      for (int i = 0; i < Orientation.NUM_ORIENTATIONS; i++)
      {
         morphognostic.orientation      = i;
         double[] responseProbabilities = classifyMorphognostic(morphognostic);
         for (int j = 0; j < NUM_RESPONSES; j++)
         {
            if ((d < 0.0) || (responseProbabilities[j] > d))
            {
               response = j;
               d        = responseProbabilities[j];
            }
            else
            {
               if (responseProbabilities[j] == d)
               {
                  if (random.nextBoolean())
                  {
                     response = j;
                     d        = responseProbabilities[j];
                  }
               }
            }
         }
      }
      morphognostic.orientation = Orientation.NORTH;
   }


   // Initialize autopilot.
   public void initAutopilot()
   {
      int w = nest.size.width;
      int h = nest.size.height;

      radius = x;
      int r = Math.abs(w - x - 1);
      if (r < radius)
      {
         radius = r;
      }
      if (y < radius)
      {
         radius = y;
      }
      r = Math.abs(h - y - 1);
      if (r < radius)
      {
         radius = r;
      }
      ring        = 0;
      step        = 0;
      orientation = Orientation.WEST;
   }


   // Autopilot response.
   void autoPilotResponse()
   {
      if (nest.cells[x][y][Nest.ELEVATION_CELL_INDEX] <
          nest.restoreCells[x][y][Nest.ELEVATION_CELL_INDEX])
      {
         response = RAISE;
      }
      else if (nest.cells[x][y][Nest.ELEVATION_CELL_INDEX] >
               nest.restoreCells[x][y][Nest.ELEVATION_CELL_INDEX])
      {
         response = LOWER;
      }
      else
      {
         int steps = ring * 2;
         switch (orientation)
         {
         case Orientation.WEST:
            if (step == (steps + 1))
            {
               if (ring < radius)
               {
                  response = TURN_LEFT;
                  ring++;
                  step = 1;
               }
               else
               {
                  response = WAIT;
               }
            }
            else
            {
               response = FORWARD;
               step++;
            }
            break;

         case Orientation.SOUTH:
            if (step == steps)
            {
               response = TURN_LEFT;
               step     = 0;
            }
            else
            {
               response = FORWARD;
               step++;
            }
            break;

         case Orientation.EAST:
            if (step == steps)
            {
               response = TURN_LEFT;
               step     = 0;
            }
            else
            {
               response = FORWARD;
               step++;
            }
            break;

         case Orientation.NORTH:
            if (step == steps)
            {
               response = TURN_LEFT;
               step     = 0;
            }
            else
            {
               response = FORWARD;
               step++;
            }
            break;
         }
      }
   }


   // Random movement.
   void randomMovement()
   {
      switch (random.nextInt(3))
      {
      case 0:
         response = FORWARD;
         return;

      case 1:
         response = TURN_LEFT;
         return;

      case 2:
         response = TURN_RIGHT;
         return;
      }
   }


   // Initialize metamorph neural network.
   public void initMetamorphNN()
   {
      metamorphNNattributeNames = new FastVector();
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
                  {
                     metamorphNNattributeNames.addElement(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
                  }
               }
            }
         }
      }
      FastVector responseVals = new FastVector();
      for (int i = 0; i < NUM_RESPONSES; i++)
      {
         responseVals.addElement(i + "");
      }
      metamorphNNattributeNames.addElement(new Attribute("type", responseVals));
      metamorphInstances = new Instances("metamorphs", metamorphNNattributeNames, 0);
      metamorphNN        = new MultilayerPerceptron();
   }


   // Create and train metamorph neural network.
   public void createMetamorphNN() throws Exception
   {
      // Create instances.
      metamorphInstances = new Instances("metamorphs", metamorphNNattributeNames, 0);
      for (Metamorph m : metamorphs)
      {
         metamorphInstances.add(createInstance(metamorphInstances, m));
      }
      metamorphInstances.setClassIndex(metamorphInstances.numAttributes() - 1);

      // Create and train the neural network.
      MultilayerPerceptron mlp = new MultilayerPerceptron();
      metamorphNN = mlp;
      mlp.setLearningRate(0.1);
      mlp.setMomentum(0.2);
      mlp.setTrainingTime(2000);
      mlp.setHiddenLayers("20");
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 20"));
      mlp.buildClassifier(metamorphInstances);

      // Save training instances?
      if (saveMetamorphInstances)
      {
         ArffSaver saver = new ArffSaver();
         saver.setInstances(metamorphInstances);
         saver.setFile(new File("metamorphInstances.arff"));
         saver.writeBatch();
      }

      // Save networks?
      if (saveMetamorphNN)
      {
         Debug.saveToFile("metamorphNN.dat", mlp);
      }

      // Evaluate the network.
      if (evaluateMetamorphNN)
      {
         Evaluation eval = new Evaluation(metamorphInstances);
         eval.evaluateModel(mlp, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
   }


   // Create metamorph NN instance.
   Instance createInstance(Instances instances, Metamorph m)
   {
      double[]  attrValues = new double[instances.numAttributes()];
      int a = 0;
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood neighborhood = m.morphognostic.neighborhoods.get(i);
         float[][][] densities = neighborhood.rectifySectorTypeDensities();
         int n = neighborhood.sectors.length;
         for (int j = 0, j2 = n * n; j < j2; j++)
         {
            for (int d = 0, d2 = m.morphognostic.eventDimensions; d < d2; d++)
            {
               for (int k = 0, k2 = m.morphognostic.numEventTypes[d]; k < k2; k++)
               {
                  attrValues[a] = densities[j][d][k];
                  a++;
               }
            }
         }
      }
      attrValues[a] = instances.attribute(a).indexOfValue(m.response + "");
      a++;
      return(new Instance(1.0, attrValues));
   }


   // Use metamorph NN to get response probability distribution.
   public double[] classifyMorphognostic(Morphognostic morphognostic)
   {
      Metamorph metamorph = new Metamorph(morphognostic, 0);

      //int       response  = 0;

      try
      {
         // Classify.
         Instance instance = createInstance(metamorphInstances, metamorph);
         //int      predictionIndex = (int)metamorphNN.classifyInstance(instance);

         // Get the predicted class label from the predictionIndex.
         //String predictedClassLabel = metamorphInstances.classAttribute().value(predictionIndex);
         //response = Integer.parseInt(predictedClassLabel);

         // Get the prediction probability distribution.
         return(metamorphNN.distributionForInstance(instance));
      }
      catch (Exception e)
      {
         System.err.println("Error classifying morphognostic:");
         e.printStackTrace();
      }
      return(null);
   }
}
