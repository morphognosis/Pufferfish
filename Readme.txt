Pufferfish nest building with Morphognosis learning and control.

A species of pufferfish builds circular nesting sites on the sea floor.
This project simulates this nest-building behavior in a cellular automaton using the Morphognosis model.

Sensory/response capabilities:
The pufferfish can sense the elevations of three cells in front of it: left, center, and right.
Responses: wait, forward, turn left, turn right, smooth sensed surface cells, raise and lower surface at current location.

Prerequesite: Java.

Setup:

1. Clone or download and unzip the pufferfish code from https://github.com/morphognosis/Pufferfish.
2. Optional: Import Eclipse project.
3. Optionally build (since it comes pre-built): click or run the build.bat/build.sh in the work folder to build the code.

Run: 
1. Click on or run the pufferfish.bat/pufferfish.sh command in the work folder to bring up the display and dashboard.
2. The dashboard is set to the "autopilot" driver, which guides the pufferfish through the process of learning
production rules. Step the pufferfish until the nest is constructed.
3. Reset the environment. 
4. Change the driver to "metamorphRules" which will utilize the learned production rules. Step the pufferfish until the nest 
is constructed again.

Manual operation:
Any sequence of responses can be performed by using the manual driver. Click the desired next response and then step to execute.

Neural network training:
Construct the nest using autopilot, then write out the training dataset using the dashboard.
The dataset can be used with your favorite machine learning tools, e.g. H2Oai (https://www.h2o.ai)

References:

Generating an artificial nest building pufferfish in a cellular automaton through behavior decomposition: 
	Paper: http://tom.portegys.com/research.html#pufferfish
	Code: https://github.com/morphognosis/Pufferfish
	
Morphognosis: the shape of knowledge in space and time:
	Paper: http://www.researchgate.net/publication/315112721_Morphognosis_the_shape_of_knowledge_in_space_and_time
	Code: https://github.com/morphognosis/Morphognosis
	
Learning C. elegans locomotion and foraging with a hierarchical space-time cellular automaton:	
	https://www.researchgate.net/publication/326832203_Learning_C_elegans_locomotion_and_foraging_with_a_hierarchical_space-time_cellular_automaton
	
Pufferfish information:
https://en.wikipedia.org/wiki/Torquigener_albomaculosus
http://www.smithsonianmag.com/smart-news/pufferfish-create-underwater-crop-circles-when-they-mate-620736
