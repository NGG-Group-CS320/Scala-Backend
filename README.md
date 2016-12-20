# MF-DAT Scala Backend #

RESTful API and health score computation engine for MF-DAT, written in Scala.

### MacOS Setup ###

1. Install the latest JDK 8 from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. Using Homebrew run, `brew install sbt`.

### Ubuntu/Debian Setup ###

1. Install the latest JDK 8 of your choosing (OpenJDK or Oracle).
2. Run the following commands to install sbt:
```bash
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```
