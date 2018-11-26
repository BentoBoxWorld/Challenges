# addon-challenges
Add-on for BentoBox to provide challenges for BSkyBlock and AcidIsland. This add-on will work
for both game modes.

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml and an example challenges.yml
4. Edit the config.yml and challenges.yml files how you want. Note that unlike ASkyBlock, the challenges.yml is for *importing only*.
5. Restart the server
6. To import challenges into BSkyBlock do /bsb challenges import. To import into AcidIsland do /acid challenges import.

## Config.yml

The config.yml has the following sections:

* Reset Challenges - if this is true, player's challenges will reset when they reset an island or if they are kicked or leave a team. Prevents exploiting the challenges by doing them repeatedly. Default is true
* Broadcast 1st time challenge completion messages to all players. Change to false if the spam becomes too much. Default is true.
* Remove non-repeatable challenges from the challenge GUI when complete. Default is false.
* Add enchanted glow to completed challenges. Default is true


## Challenges.yml

This file is just to facilitate importing of old ASkyBlock or AcidIsland challenges and is not used during the normal operation of the game. it is meant to just enable you to jump start your challenge collection.

This file format is very similar to the ASkyBlock file but not exactly the same because it is designed for 1.13.x servers and higher. If you try to import ASkyBlock challenges, they may or may not completely import, so check for errors in the console. 

Once you have imported challenges, the *real* challenge files are actually in two folders in the BentoBox database folder. One folder is for the challenges and the other is for the challenge levels. They are all defined in .yml files in these locations:

```
plugins/BentoBox/database/Challenges
plugins/BentoBox/database/ChallengeLevels
```

If you edit a file, then you should reload the challenge database by using the admin reload command, e.g. **/bsb challenges reload** or **/acid challenges reload**.

If you want to force an overwrite of challenges via an import, add the **overwrite** option to the end of the import command.

Note that you must import challenges into both BSkyBlock and AcidIsland separately.


## Admin commands

There are a few admin commands and more being written. The main challenge admin command is **/bsb challenges** or **/acid challenges**. Use 

* /bsbadmin challenges help : Show help for all the commands
* /bsbadmin challenges import [overwrite]: import challenges from challenges.yml
* /bsbadmin challenges complete <player> <unique challenge name>: Mark challenge complete
* /bsbadmin challenges reload : reload challenges from the database
* /bsbadmin challenges reset <player> <unique challenge name>: Reset challenge to 0 times / incomplete





