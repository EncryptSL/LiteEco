# LiteEco - Plugin Docs

## Table of Contents
- [Setup](CONFIG.md)
- [Permissions](PERMISSION.md)
- [Placeholders](PLACEHOLDER.md)
- [Translations](#translation)
- [Contribution](#contribution-guide)

## [Setup](CONFIG.md)
The configuration file (**config.yml**) is where you can customize the settings for the LiteEco plugin to suit your specific needs **. . .**

## [Permission](PERMISSION.md)
LiteEco provides various permissions for player and admin commands to control access to specific features **. . .**

## [Placeholder](PLACEHOLDER.md)
LiteEco uses placeholders in its output to dynamically display various information **. . .**

## Translation
LiteEco supports translations, allowing the plugin to be used in different languages. 
The translations are based on Enums, and you can find the list of available locales [here](https://github.com/EncryptSL/LiteEco/blob/main/src/main/kotlin/encryptsl/cekuj/net/api/enums/LangKey.kt).

### How to Contribute Translations

If you want to contribute with another translation, follow these steps:

1. Fork this repository to your GitHub account.

2. Create a new branch with a descriptive name for your translation work. (e.g. `french-translation`)

3. Create a translation file with the format `locale_key.yml`, using the correct [locale code](https://www.ibm.com/docs/en/radfws/9.6.1?topic=overview-locales-code-pages-supported) for the language you are translating to.

4. Inside the translation file, make use of the placeholders provided in the original files, such as `<example_something>`. 

5. Ensure not to remove or modify the placeholders, as they are used for variable substitution during runtime.

6. Feel free to customize the colors or any other elements in the translation to match the language's conventions and style.

7. Once your translation is ready, add it to the [locale folder](https://github.com/EncryptSL/LiteEco/tree/main/src/main/resources/locale) in the plugin's repository.


## Contribution Guide

If you would like to contribute to the LiteEco project, follow the guide below

### Getting Started for Coding

Ensure you have the necessary prerequisites, including **IntelliJ IDEA** and **Git** or **GitHub Desktop**.

To get started with git, clone the LiteEco repository to your local machine using the following command:

```bash
git clone https://github.com/EncryptSL/LiteEco.git
```

Wait until the project download is successful.

### Making Changes
Once you have cloned the repository, you can make your changes or additions to the code.

### Code Formatting
Before submitting a pull request, please ensure that your code is properly formatted and follows the project's coding style guidelines.

### Optimized Imports
Make sure to check and optimize the imports in your code to keep the project organized.

### Submitting a Pull Request
After you have made your changes and ensured that your code is properly formatted, you can submit a pull request.

We appreciate contributions, and thank you for your interest in improving the project!
If you have any questions or need assistance, feel free to reach out to us.