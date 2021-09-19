# Contributing

We welcome any and all contributions to Forecastie. Please add suggestions, bugs or requests for new features to the [Issues](https://github.com/martykan/forecastie/issues) page. The [Issues](https://github.com/martykan/forecastie/issues) page is also the best place to ask questions about the code, the structure of the software, release schedule, etc.

## Pull requests

We also accept pull requests; anything you submit will be reviewed and discussed, and merged if appropriate. Please take the time to read relevant [Issues](https://github.com/martykan/forecastie/issues), or open one yourself on the topic.

## Code guidelines

Although we will discuss every pull request, we'd like you to prepare your contribution as well as possible. Keep your code easy to understand and maintainable as well as consistent with the existing code. Add helpful comments or JavaDoc when necessary.

We don't have a prescribed code style but you can find reasonable guidelines in the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) and the [Android Contributors Code Style](https://source.android.com/setup/contribute/code-style).

We currently support devices which run Android version 4.0.3/API 15. We will continue this so those with older Android phones may use the software. Please bear this in mind when preparing code for a pull request.

## Translations

It is very important to have Forecastie available in as many languages as possible: we currently offer 27 languages beside the default English, unfortunately they are not always as up-to-date as they should be. All translators and their contributions are very welcome, please do whatever you can, no matter how small the change. We have suspended the Transifex page, as we no longer have access. However, all remaining strings will be copied across manually.

The translation strings for the various languages are located in `strings_*.xml` files inside the [`app/src/main/res/values-*`](./app/src/main/res/) folders. The translation strings are organized in several files such as `strings_notification.xml` and `strings_settings_units.xml` depending on their purpose. For your translation please follow the same file structure as the English translation in [`app/src/main/res/values`](./app/src/main/res/values). Please note that you should only create the `strings_*.xml` files, the other files in `app/src/main/res/values` are not related to translations.

There is also a [Python3 script](./tools/translation-progress.py) to help in your translation efforts. Some useful commands:

``` sh
# Show the avialable options.
./tools/translation-progress.py --help

# Show an overview of the translation status for all languages.
./tools/translation-progress.py -v

# Show an overview of the translation status for Swedish.
./tools/translation-progress.py -v se

# Show an overview of the translation status for Korean. Also include
# information about misplaced translation strings, meaning those that are
# present but not in the correct `strings_*.xml` file.
./tools/translation-progress.py -v -m ko

# Show an overview of the translation status for Greek and which strings still
# need to be translated.
./tools/translation-progress.py -vv el
```

Detailed documentation ofthe script's output is located in the form of comments in the beginning of the script.

