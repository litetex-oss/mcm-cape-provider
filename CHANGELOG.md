# 2.0.0
* Try to utilize Fabric's Rendering API - if present - to prevent conflicts with other mods
* Make it possible to control animated textures
  * ON (default)
  * Frozen = Textures are not animated
  * OFF = Animated textures are ignored
* Refresh the capes of all players after changing the configuration
* Provide an option to only load your cape (and ignore other players)
* New debug options
   * block certain capes by provider
   * amount of Cape-Loader Threads (default: 2)
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
