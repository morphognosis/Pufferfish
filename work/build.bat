javac -classpath "../lib/morphognosis.jar;../lib/weka.jar" -d . ../src/morphognosis/pufferfish/*.java
copy ..\res\images\pufferfish_nest.png morphognosis\pufferfish
jar cvfm ../bin/pufferfish.jar pufferfish.mf morphognosis
