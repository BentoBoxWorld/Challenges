# Challenges Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Challenges)](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/)

Add-on for BentoBox to provide challenges for any BentoBox GameMode. 

## Where to find

Currently Challenges Addon is in **Beta stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
Latest official **Beta Release is 0.7.5**, and you can download it from [Release tab](https://github.com/BentoBoxWorld/Challenges/releases)
But it will work with BentoBox 1.5.0 only.

Latest development build will work with **BentoBox 1.7.0**, as it contains new functionality, that is not in 1.6 release.
**Nightly builds** are available in [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/lastStableBuild/).

Be aware that 0.8.0-SNAPSHOT stores data differently than it is in 0.7.5 and below. It will be necessary to migrate data via command "/[gamemode_admin] challenges migrate".

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Challenges/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

## Translations

As most of BentoBox projects, Challenges Addon is translatable in any language. Everyone can contribute, and translate some parts of the addon in their language via [GitLocalize](https://gitlocalize.com/repo/2896).
If your language is not in the list, please contact to developers via Discord and it will be added there.
Unfortunately, default challenges come only in English translation. But with version 0.8.0 there will be access to different challenges libraries, where everyone could share their challenges with their translations. More information will come soon.

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. Edit the config.yml how you want.
4. Restart the server

#### Challenges

By default, challenges addon comes without any challenge or level. On first runtime only Admin GUI will be accessible. 
Admins can create their own challenges or import some default challenges, which importing also are available via Admin GUI. Default challenges contains 5 levels and 57 challenges.

## Compatibility

- [x] BentoBox - 1.7.0 version
- [x] BSkyBlock - 1.6.0 version
- [x] AcidIsland - 1.6.0 version
- [x] SkyGrid - 1.6.0 version
- [x] CaveBlock - 1.6.0 version

## Config.yml

As most of BentoBox addons, config can be edited only when server is stopped. Otherwise all changes will be overwritten by server.
The config.yml has the following sections:

* **Commands** - ability to enable */challenges* command. This option change is possible only via configuration and requires server restart.
		To enable, you should change `single-gui` to `true`.
* **History** - ability to enable completion history storing in player data object. 
		To enable, you should change `store-history-data` to `true`.
		It is possible to change life-span of history data in days. (0 means that data will not be removed)
* **GUI Settings** - ability to change some options that are visible only in challenges GUI.
	* Remove non-repeatable challenges from the challenge GUI when complete. Default is false.
	* Add enchanted glow to completed challenges. Default is true.
	* Locked level icon is displayed for locked levels.
	* Free challenges location - You can decide, either free challenges will be at the top, or at the bottom.
	* Description line length - allows to specify maximal line length in GUI icon descriptions.
	* Challenge Description structure - allows to modify structure of challenge description.
    * Level Description structure - allows to modify structure of Level description.
* **Store mode** - ability to store challenges completion per island or per player.
		To enable storing challenges data per island change `store-island-data` to `true`. ATTENTION: progress will be lost on this option change.
* **Reset Challenges** - if this is true, player's challenges will reset when they reset an island or if they are kicked or leave a team. Prevents exploiting the challenges by doing them repeatedly. Default is true
* **Broadcast** - ability to broadcast 1st time challenge completion messages to all players. Change to false if the spam becomes too much. Default is true.
* **Title** - ability to enable showing Title screen on first challenge completion or level completion.
* **Disabled GameModes** - specify Game Modes where challenges will not work.

## Information

More information can be found in [Wiki Pages](https://github.com/BentoBoxWorld/Challenges/wiki).
