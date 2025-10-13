# 4.1.1
* Update Cosmetica API #166
* Improved mod compatibility

# 4.1.0
* Updated to 1.21.9

# 4.0.2
* Fixed: `$` inside `fabric.mod.json` is usually replaced by the build process #145
  * Added a note that this may require escaping using a `\`
  * Made it possible to use `§` instead of `$`
    * Updated the demo accordingly

# 4.0.1
* Made animated GIF decoding more reliable
  * This should fix broken textures #135
* The maximum size for image downloads is now 10MB by default

# 4.0.0
* Implemented Texture Resolvers
  * Added support for GIFs #131
  * Deprecated `animated`-flag: Use `textureResolverId` instead
  * For further details please have a look at the corresponding documentation on the README
* Various small code improvements

# 3.0.0
* Other mods can now define cape providers in their metadata #94
  * This way other developers don't have to manually implement/copy-paste code for adding custom capes (e.g. for supporters) which should result in less conflicts
  * Cape providers loaded from other mod's metadata are automatically loaded and activated by default
    * This can be changed in the settings under ``Other > Providers from mods``
  * Usage example:
    * Simple implementation:<br/>
      ``fabric.mod.json``
      ```json5
      {
        ...
        "custom": {
          "cape": "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png"
        }
      }
      ```
    * More detailed variant:<br/>
      ``fabric.mod.json``
      ```json5
      {
        "custom": {
          "cape": {
            // Gives everyone a christmas cape
            // You can also use variables here, like $uuid. See above for more details
            // Alternative: "uriTemplate"
            "url": "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png",
            "changeCapeUrl": "https://...",
            "rateLimitedReqPerSec": 20
          }
        }
      }
      ```

# 2.3.0
* Make it possible to disable the default/Minecraft provider #64
* Improved mod compatibility

# 2.2.0
* Improved real player detection
  * Capes are now only loaded for players with a valid UUID
* Improved load balancing with cape providers
  * Providers are now rate limited
    * the default is 20 req/s
    * can be overwriten per provider using ``rateLimitedReqPerSec``
  * If there are too many pending cape load tasks the oldest ones will now be automatically discarded
* Limited amount of tracked players to prevent running out of memory
* Now compatibile with ``SkinShuffle``
* Now targeting 1.21.8

# 2.1.0
* Fixed mod configuration button not being displayed correctly in Skin Customization Screen when resizing #79
* Fully utilize fabric-api. fabric-api is now required #78
* Now targeting 1.21.7

# 2.0.1
* Use built-in warning icon instead of bringing a custom one #66
* Also publish to CurseForge #62
  * Available (once CurseForge reviewed it) at https://www.curseforge.com/minecraft/mc-mods/cape-provider

# 2.0.0
* New options
  * Control over animated textures
    * ON (default)
    * Frozen = only the first frame of the animation is displayed
    * OFF = Animated textures are ignored
  * Only load your cape (and ignore other players)
* New debug options
   * block certain capes by provider
   * amount of Cape-Loader Threads (default: 2)
* Now refreshes the capes of all players when the configuration is changed
* Utilizes Fabric's Rendering API - if present - to prevent conflicts with other mods
* Various minor improvements and optimizations

# 1.2.0
* Updated to 1.21.6
  * Reworked/Fixed preview rendering
* Custom providers (loaded from the config-file) can now provide animated textures

# 1.1.1
* Optimized rendering in preview screen
* Correctly declared fabric-api

# 1.1.0
* Updated to 1.21.5

# 1.0.2
* Distribute mod on Maven Central

# 1.0.1
* Improved compatibility information

# 1.0.0
Improvements and changes in comparison to Capes mod:
* Improved provider configuration screen
  * Can now be ordered
  * Added links for homepage / to cape editor
* Fixed preview rendering
* Built-in providers:
  * MinecraftCapes (active by default)
  * OptiFine (active by default)
  * Wynntils
    * Fixed capes not being loaded
  * Cosmetica
  * LabyMod
    * Prevented default cape from always being displayed
* Added Anti-Feature notices
* Added support for custom providers to the config
* Added reset function
* Improved provider responsiveness
  * Implemented basic caching
  * Implemented check if player is real (and not simulated by the server)
* Various other improvements
