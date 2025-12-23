<!-- modrinth_exclude.start -->

[![Version](https://img.shields.io/modrinth/v/cape-provider)](https://modrinth.com/mod/cape-provider)
[![Build](https://img.shields.io/github/actions/workflow/status/litetex-oss/mcm-cape-provider/check-build.yml?branch=dev)](https://github.com/litetex-oss/mcm-cape-provider/actions/workflows/check-build.yml?query=branch%3Adev)

# Cape Provider

<!-- modrinth_exclude.end -->

<img align="right" src="https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/src/main/resources/assets/icon.png" width=192 />

Provides you with capes!

You can choose from various providers or add your own.

Improved/Reworked version of the ["Capes" mod](https://github.com/CaelTheColher/Capes):
* Improved and easier cape provider integration
* Allows ordering providers
* Support for custom providers
* More options to fine tune how capes are applied
* Written only in Java (no Kotlin needed)
* Various fixes and improvements

<img align="right" src="https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/assets/config-preview2.jpg" width=240 />

### Creating a custom cape provider

The mod provides many different ways how a provider can be added.

The following possibilities are sorted by simplicity:

#### Simple Local Provider

> Recommended for:
> * Users that just want a customizable cape 
> * Modpacks (using `config/cape-provider/simple-custom`)

The simples way to display a cape is by going into the `config/cape-provider` directory and creating a cape texture file named `cape.png`.

Additionally there are the following optional files:
* `owners.txt` - Determines which player names or UUIDs will get the cape displayed. If this file is not present then all players will display with the cape.
* `name.txt` - To override the display name of the provider

You can also add more providers by creating corresponding directories in `config/cape-provider/simple-custom`.<br/>Example: `config/cape-provider/simple-custom/my-super-cool-provider/cape.png`

#### Remote Provider in configuration

> Recommended for:
> * Users that want to add a custom remote provider

This demo showcases how to apply the capes inside [``custom-cape-demo``](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/custom-cape-demo).

1. Open the config file located in ``config/cape-provider/config.json``
2. In the ``remoteCustomProviders`` section add the following entry:
    ```jsonc
    {
      "id": "cp1",
      "name": "CustomProvider1",
      // You can replace uuid with $id, $name or $idNoHyphen to customize the cape per Player
      "uriTemplate": "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png"
    }
    ```
    <details><summary>Example for SkinMC</summary>

    ```jsonc
    {
      "id": "skinmc",
      "name": "SkinMC",
      "uriTemplate": "https://skinmc.net/api/v1/skinmcCape/$id"
    }
    ```

    </details>
3. Restart the game and activate the provider

For more details have a look at [RemoteCustomProvider](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/provider/custom/remote/RemoteCustomProvider.java) and [RemoteCustomProviderConfig](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/provider/custom/remote/RemoteCustomProviderConfig.java)

NOTE: Texture resolvers can be selected using the `textureResolverId` attribute (see below for details).

#### via Mods

> Recommended for:
> * Mods

If you are a mod developer and want to e.g. display a cape for supporters or contributors of your mod, you can provide it using the mod's resources and/or metadata in ``fabric.mod.json``.
The overall behavior is similar to how [``modmenu``](https://github.com/TerraformersMC/ModMenu?tab=readme-ov-file#fabric-metadata-api) handles this.

##### Local/Simple (Recommended)

This approach requires no network communication and is the recommended approach.
It works by reading metadata and resources from the `cape` directory.

Here is an example:
1. Add the following mod metadata:
    ``fabric.mod.json``
    ```json5
    {
      ...
      "custom": {
        "cape": "Contributors"
      }
    }
    ```
2. Create a `cape` directory inside `resources`
3. Add the cape texture in `cape/cape.png`
4. Add the players that should be given the cape in `cape/owners.txt` with their UUIDs or names

<details><summary>Note: There is also a more detailed variant</summary>

``fabric.mod.json``
```json5
{
  "custom": {
    "cape": {
      "name-extra": "Contributors",
      "owners": {
        // You can also used UUIDs
        "names": [
          "Notch"
        ]
      }
    }
  }
}
```

</details>

The mod uses this strategy itself. See the [`fabric.mod.json`](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/resources/fabric.mod.json) or [`cape` directory](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/resources/cape) for details.

##### Remote

Here's an example implementation that shows how a remote cape provider can be added:

``fabric.mod.json``
```json5
{
  ...
  "custom": {
    "cape": "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png"
  }
}
```

<details><summary>Here's a more detailed variant</summary>

``fabric.mod.json``
```json5
{
  "custom": {
    "cape": {
      // Gives everyone a christmas cape
      // You can also use variables here, like $uuid. See above for more details
      // You may have to escape the $ with \ or you can alternatively use § instead of $
      // Alternative: "uriTemplate"
      "url": "https://example.org/textures/§uuid.png",
      "changeCapeUrl": "https://...",
      "rateLimitedReqPerSec": 20 // Default is 20
    }
  }
}
```

</details>

#### Programmatic

You can also create a [programmatic cape provider](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/PROGRAMMATIC_PROVIDER.md).

### Further notes

#### Maximum size

Images/Textures should not exceed 10MB. Otherwise they might be ignored.

#### Texture resolvers / Animated textures

The following resolvers are currently built-in:

| Resolver-ID | Animated | Format | Example | Notes |
| --- | --- | --- | --- | --- |
| `default` / null | ❌ | [PNG](https://de.wikipedia.org/wiki/Portable_Network_Graphics) | [uuid.png](https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png) | |
| `sprite` | ✔ | Stacked [PNG](https://de.wikipedia.org/wiki/Portable_Network_Graphics) | [animated.png](https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/animated.png) | |
| `gif` | ✔ | [GIF](https://de.wikipedia.org/wiki/Graphics_Interchange_Format) | [animated.gif](https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/animated.gif) | _Usage not recommended_<br/>GIFs require more resources when compared to more modern formats like PNG. |

Please note that animated textures can be frozen or completely disabled in the settings.

<!-- modrinth_exclude.start -->

## Installation
[Installation guide for the latest release](https://github.com/litetex-oss/mcm-cape-provider/releases/latest#Installation)

### Usage in other mods

Add the following to ``build.gradle``:
```groovy
dependencies {
    modImplementation 'net.litetex.mcm:cape-provider:<version>'
    // Further documentation: https://wiki.fabricmc.net/documentation:fabric_loom
}
```

> [!NOTE]
> The contents are hosted on [Maven Central](https://repo.maven.apache.org/maven2/net/litetex/mcm/). You shouldn't have to change anything as this is the default maven repo.<br/>
> If this somehow shouldn't work you can also try [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven).

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

<!-- modrinth_exclude.end -->
