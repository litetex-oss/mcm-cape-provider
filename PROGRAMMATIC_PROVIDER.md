# Creating a custom programmatic cape provider

> [!NOTE]
> This is more complex and some Java coding experience is required.

You can also add a custom cape provider programmatically using [Java's Service Loading](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html).

To achieve the same as in the example [from the Readme](./README.md) do the following:

1. Create a new (fabric) mod
2. Add the mod as a dependency:
    ``build.gradle``
    ```groovy
    dependencies {
        modImplementation 'net.litetex.mcm:cape-provider:<version>'
    }
    ```
3. Create a custom provider:
    ``src/main/java/com/example/capes/provider/MyCustomCapeProvider.java``
    ```java
    public class MyCustomCapeProvider implements CapeProvider
    {
        @Override
        public String id() {
            return "cp";
        }

        @Override
        public String name() {
            return "Custom Provider";
        }

        @Override
        public String getBaseUrl(GameProfile profile) {
            return "https://raw.githubusercontent.com/litetex-oss/mcm-cape-provider/refs/heads/dev/custom-cape-demo/uuid.png";
        }

        // You can add more custom code here
    }
    ```
4. Register the provider
    ``src/main/resources/META-INF/services/net.litetex.capes.provider.CapeProvider``
    ```
    com.example.capes.provider.MyCustomCapeProvider
    ```
5. (Optional) Declare that your mod needs Cape Provider
    ``src/main/resources/fabric.mod.json``
    ```jsonc
      // ...
      "recommends": { // You can also use "depends"
        "cape-provider": "*" // Or add the corresponding version
      }
      // ...
    ```
    You might also have to declare this in other places (e.g. on Modrinth).
