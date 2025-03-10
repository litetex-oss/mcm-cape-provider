<!-- modrinth_exclude.start -->

[![Version](https://img.shields.io/modrinth/v/cape-provider)](https://modrinth.com/mod/cape-provider)
[![Build](https://img.shields.io/github/actions/workflow/status/litetex-oss/mcm-cape-provider/check-build.yml?branch=dev)](https://github.com/litetex-oss/mcm-cape-provider/actions/workflows/check-build.yml?query=branch%3Adev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=litetex-oss_mcm-cape-provider&metric=alert_status)](https://sonarcloud.io/dashboard?id=litetex-oss_mcm-cape-provider)

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

For more details have a look at [CustomProvider](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/provider/CustomProvider.java) and [CustomProviderConfig](https://github.com/litetex-oss/mcm-cape-provider/tree/dev/src/main/java/net/litetex/capes/config/CustomProviderConfig.java)

<!-- modrinth_exclude.start -->

## Installation
[Installation guide for the latest release](https://github.com/litetex-oss/mcm-cape-provider/releases/latest#Installation)

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

<!-- modrinth_exclude.end -->
