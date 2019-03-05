# Challenges Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Challenges)](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/)

Add-on for BentoBox to provide challenges for any BentoBox GameMode. 

## Where to find

Currently Challenges Addon is in **Beta stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
Latest official **Beta Release is 0.6.1**, and you can download it from [Release tab](https://github.com/BentoBoxWorld/Challenges/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Challenges/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml and an example challenges.yml
4. Edit the config.yml and challenges.yml files how you want. Note that unlike ASkyBlock, the challenges.yml is for *importing only* and faster start.
5. Restart the server
6. To import challenges into GameMode, you must run admin command and attach `challenges import` at the end. Or you can use challenges admin GUI to do the same.

## Compatibility

- [x] BentoBox - 1.3.0 version
- [x] BSkyBlock - 1.3.0 version
- [x] AcidIsland - 1.3.0 version
- [x] SkyGrid - 1.3.0-SNAPSHOT version
- [ ] CaveBlock

## Config.yml

As most of BenotBox addons, config can be edited only when server is stopped. Otherwise all changes will be overwritten by server.
The config.yml has the following sections:

* Reset Challenges - if this is true, player's challenges will reset when they reset an island or if they are kicked or leave a team. Prevents exploiting the challenges by doing them repeatedly. Default is true
* Broadcast 1st time challenge completion messages to all players. Change to false if the spam becomes too much. Default is true.
* Remove non-repeatable challenges from the challenge GUI when complete. Default is false.
* Add enchanted glow to completed challenges. Default is true
* Free challenges location - You can decide, either free challenges will be at the top, or at the bottom.
* Description line length - allows to specify maximal line length in GUI icon descriptions.
* Challenge Description structure - allows to modify structure of challenge description.
* Level Description structure - allows to modify structure of Level description.
* Disabled GameModes - specify Game Modes where challenges will not work.

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
* /bsbadmin challenges reload : reload challenges from the database
