# Battlecode 2022 Scaffold

This is the Battlecode 2022 repository for Team Ruby Grapefruit. The team was composed of Daniel Hopping (previously a member of [Oak's Last Disciple](https://github.com/IvanGeffner/BC19) and [Java Best Waifu](https://github.com/IvanGeffner/battlecode2020)), as well as first-time participants William Adamson and Braden Honea. They narrowly missed out on the final tournament by placing 13th-16th in US Qualifying. The final game against Team Code Monkeys can be seen [here](https://youtu.be/giZi8DKS1cM?t=10525). 

Read https://play.battlecode.org/getting-started!

### Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.


### Useful Commands

- `./gradlew run`
    Runs a game with the settings in gradle.properties
- `./gradlew update`
    Update to the newest version! Run every so often

