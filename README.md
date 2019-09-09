# Challenges Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Challenges)](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/)

Add-on for BentoBox to provide challenges for any BentoBox GameMode. 

## Where to find

Currently Challenges Addon is in **Beta stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
Latest official **Beta Release is 0.8.0**, and you can download it from [Release tab](https://github.com/BentoBoxWorld/Challenges/releases)
But it will work with BentoBox 1.6.x and BentoBox 1.7.x.

Latest development builds will be based on **Minecraft 1.14.4** and **BentoBox 1.8.0**.
**Nightly builds** are available in [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/lastStableBuild/).

Be aware that 0.8.0 stores data differently than it is in 0.7.5 and below. It will be necessary to migrate data via command `/[gamemode_admin] challenges migrate`.

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
There exist also Web Library, where users can download public challenges. It is accessible with Admin GUI by clicking on Web icon.

## Compatibility

- [x] BentoBox - 1.6.x and 1.7.x versions
- [x] BSkyBlock
- [x] AcidIsland
- [x] SkyGrid 
- [x] CaveBlock

## Information

More information can be found in [Wiki Pages](https://github.com/BentoBoxWorld/Challenges/wiki).
