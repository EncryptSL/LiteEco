<div style="text-align: center;">

## LiteEco - Translators

![banner](https://user-images.githubusercontent.com/9441083/215228544-29c3bfa3-f67f-4f9d-8510-bde3f133018e.jpg)

## Translations

</div>

---

LiteEco supports translations, allowing the plugin to be used in different languages.

The translations are based on Enums, and you can find the list of available locales [**here**](https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt).

<div style="text-align: center;">

## How to Contribute Translations

</div>

---

If you want to contribute with another translation, follow these steps:

1. Fork this repository to your GitHub account.

2. Create a new branch with a descriptive name for your translation work. (e.g. `french-translation`)

3. Create a translation file with the format `locale_key.yml`, using the correct [**locale code**](https://www.ibm.com/docs/en/radfws/9.6.1?topic=overview-locales-code-pages-supported) for the language you are translating to.

4. Inside the translation file, make use of the placeholders provided in the original files, such as `<example_something>`.

5. Ensure not to remove or modify the placeholders, as they are used for variable substitution during runtime.

6. Feel free to customize the colors or any other elements in the translation to match the language's conventions and style.

7. Once your translation is ready, add it to the [**locale folder**](https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale) in the plugin's repository.
