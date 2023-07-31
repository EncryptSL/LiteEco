<div align=center>

## LiteEco - Translations

[![Banner]](https://github.com/EncryptSL/LiteEco)

#### Multilingual support
</div>

---

LiteEco supports translations, allowing the plugin to be used in different languages.

Translations in LiteEco are built on [Enums][Enums], and you can find a list of available locales [here][Locales]. 

These locales represent different language codes supported by the plugin.

<div align=center>

## How to Contribute Translations
</div>

---

LiteEco welcomes community contributions to expand its language support. 

If you wish to add a translation for a language not yet covered or make improvements, follow these simple steps:

1. Fork this repository to your GitHub account.

2. Create a new branch with a descriptive name for your translation work, such as `french-translation`

3. Generate a translation file with the format `locale_key.yml`, using the appropriate [**locale code**][ISO Standard] for the target language.

4. Inside the translation file, make use of the placeholders provided in the original files, such as `<example_something>`.

5. Ensure not to remove or modify the placeholders, as they are used for variable substitution during runtime.

6. Feel free to customize the colors or any other elements in the translation to match the language's conventions and style.

7. Once your translation is ready, add it to the [**locale folder**][Locales] in the plugin's repository.

8. After making your changes, make a New Pull Request to submit your contribution.

[Banner]: https://i.ibb.co/gvpv3CX/LiteEco.jpg

[Enums]: https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt
[Locales]: https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale
[ISO Standard]: https://www.ibm.com/docs/en/radfws/9.6.1?topic=overview-locales-code-pages-supported
