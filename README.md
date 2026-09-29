# Cheats MC (Minecraft 26.2)

Cheats MC is a Fabric client mod based on the user supplied **Night-Mainhand-AfterPops-R1** source archive. This repository contains the source for the public build and the corresponding JAR is attached to the release.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API 0.161.0+26.2
- Java 25

## Install

Put the release JAR and Fabric API in the Minecraft 26.2 Fabric `mods` folder. Keep only one Cheats MC JAR in that folder.

## Build

On Windows with JDK 25, run `gradlew.bat clean build`. The output is `build/libs/Cheats-MC-26.2-R1.jar`.

## Changes from the supplied Night source

- Changed the displayed mod, menu, HUD, and other client branding to Cheats MC. The internal `night` package and resource namespace remain for compatibility.
- Replaced the mod icon and in-game logo with the supplied teal logo.
- Set the Discord RPC UID text to `30th6_`.
- Added a Fabric startup entry point that opens the supplied YouTube, Discord, and TikTok links in the default browser each time the game starts.

Discord RPC still uses the two application IDs and remote asset keys from the supplied Night source. The Discord artwork cannot be replaced by a local JAR image alone. It needs an asset registered with a Discord application and an updated application ID.

## Source provenance

The starting archive was `Night-Mainhand-AfterPops-R1-source.zip` (SHA-256 `ED48FDA82DE32A680EF6AB71753442F1E3853F52C1FDAA805A54E0FA540EAE5A`). The archive includes Java files with CFR decompilation headers and a Gradle project. Previous build notes inside the archive were excluded from this repository because they describe older AutoShop revisions.

The supplied `fabric.mod.json` declares `GPL-3.0`, while the supplied `src/main/resources/LICENSE_night` contains an MIT notice. Both upstream notices are preserved in the source. This repository does not resolve that inconsistency.
