#!/bin/csh
# To run JavaTrek in a screen, uncomment the below.
# screen -wipe
# screen -dmS trek /usr/lib/java/bin/java -cp lib/bots.jar:lib/javatrek.jar:lib/mysql-connector-java-5.0.5-bin.jar org.gamehost.jtrek.javatrek.TrekMain 

# Comment this line if you are running JavaTrek in a screen.
java -cp lib/bots.jar:lib/javatrek.jar:lib/mysql-connector-java-5.0.5-bin.jar org.gamehost.jtrek.javatrek.TrekMain
