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
* Written only in Java (no Kotlin needed)
* Various fixes and improvements

<img align="right" src="https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/assets/config-preview2.jpg" width=240 />

### Creating a custom cape provider

This demo showcases how to apply the capes inside [``custom-cape-demo``](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/custom-cape-demo).

1. Open the config file located in ``config/cape-provider.json5``
2. In the ``customProviders`` section add the following entry:
    ```jsonc
    {
      "id": "cp1",
      "name": "CustomProvider1",
      // You can replace uuid with $id, $name or $idNoHyphen to customize the cape per Player
      "uriTemplate": "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png"
    }
    ```
3. Restart the game and activate the provider

### Custom Provider Examples
```jsonc
    {
      "id": "skinmc",
      "name": "SkinMC",
      // This custom provider adds support for skinmc capes: https://skinmc.net
      "uriTemplate": "https://skinmc.net/api/v1/skinmcCape/$id"
    }
```

For more details have a look at [CustomProvider](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/provider/CustomProvider.java) and [CustomProviderConfig](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/config/CustomProviderConfig.java)

You can also create a [programmatic cape provider](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/PROGRAMMATIC_PROVIDER.md).

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
