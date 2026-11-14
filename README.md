# Challenges Addon
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/Challenges)](https://ci.codemc.org/job/BentoBoxWorld/job/Challenges/)

Add-on for BentoBox to provide challenges for any BentoBox GameMode. Challenges can be to collect items, to have items or blocks nearby, to amass an amount of money or island levels, or to have accomplished some player statistic. Each challenge can reward the player with money, experience points, blocks, items, or other things, like permissions, Pre-built challenges are available and there is also a Web Library of pre-built ones. Customize them to fit your needs!

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. Run the admin challenges command for your game mode, e.g. `/bsbadmin challenges`, to open the Admin GUI
4. Click **Library** and pick a challenge pack for your game mode (see below)
5. Players can now use `/island challenges` (or the equivalent for your game mode)

#### Getting challenges

The addon comes without any active challenges or levels, so until an admin adds some only the Admin GUI is useful. There are four ways to add them:

- **Web Library (recommended)**: click **Library** in the Admin GUI to download a ready-made pack. Packs exist for BSkyBlock, AcidIsland, Poseidon and Gusher, most of them in several languages. The Library only installs into a game mode that has no challenges yet, so use **Challenge Wipe** first if you are switching packs.
- **Bundled defaults**: click **Import Database** and choose `default` (5 levels and 57 challenges). Works without internet access.
- **Template file**: write challenges in YAML (see `template.yml`) and load them with **Import Template**.
- **By hand**: use **Add Challenge** and **Add Level** in the Admin GUI.

Step-by-step instructions are in the [documentation](https://docs.bentobox.world/en/latest/addons/Challenges/#getting-started).

## Compatibility

- [x] BentoBox
- [x] BSkyBlock
- [x] AcidIsland
- [x] SkyGrid 
- [x] CaveBlock

## Translations

The Challenges Addon is available in 15 languages. `en-US.yml` is the reference locale; the other locale files are generated from it and then corrected by server admins and translators.
If you spot a wrong or awkward translation, or want a language added, please open an [issue](https://github.com/BentoBoxWorld/Challenges/issues) or a pull request against the files in `src/main/resources/locales/`, or let us know on Discord.
The default challenges bundled with the addon are in English only, but the Web Library has challenge packs in several other languages.

## Documentation

More information can be found in the docs: https://docs.bentobox.world/en/latest/addons/Challenges/

## Bugs or feature requests
If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Challenges/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

