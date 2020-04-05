#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: 2020 Sotiris Papatheodorou
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Show the translation progress for each language in Forecastie. All languages
# are compared to English which is considered the original language of the
# program.
#
# Each translatable string is put in one of three categories:
# - Translated: The string is present in both the English and the other
#               language translations and its value in the other language
#               translation is different than in English.
# - Not translated: The string is present in both the English and the other
#                   language translations but its value in the other language
#                   translation is the same as in English.
# - Missing: The string is present only in the English translation and not in
#            the other language translation.
#
# Strings with the translatable="false" attribute or whose names are in the
# string_blacklist are ignored.
#
# There are two output formats:
# - CSV: Machine readable, can be used in CI scripts. This is the default
#        output format. It contains the following columns:
#        - Language       The name of the language.
#        - Filename       The path to the respective strings.xml.
#        - Translated     The number of translated strings.
#        - Not Translated The number of not translated strings.
#        - Missing        The number of missing strings.
#        - Completion     The percentage of translated strings.
# - Human readable: This format is not as structured as CSV but is easier for
#                   humans to read and provides some extra information. This
#                   format is used when increasing the verbosity level. The
#                   information shown per verbosity level:
#                   - 1 The same information as CSV plus percentages for
#                       translated, not translated and missing.
#                   - 2 The same information as verbosity level 1 plus the
#                       names of strings that are not translated or missing.
#                   - 3 The same information as verbosity level 2 plus the
#                       names of strings that are translated.

from glob import glob
from os import path
from sys import exit
from typing import Dict, List
import argparse
import xml.etree.ElementTree as ET



# These paths are relative to the script and should be changed accordingly if
# the script is moved
english_translation = '../app//main/res/values/strings.xml'
other_translations = '../app/src/main/res/values-*/strings.xml'

# String names in this list will not be considered
string_blacklist = [
        'setting_available_simple_string_codes',
        'setting_example_simple_string',
        'dash_clock_status',
        'dash_clock_expanded_title'
]



def parse_arguments():
    """
    Parse command line arguments.

    :return: A Namespace containing the arguments and their values.
    :rtype: argparse.Namespace
    """
    parser = argparse.ArgumentParser(description='Show translation progress for Forecastie')
    parser.add_argument('--verbose', '-v', action='count', default=0,
                        help='Produce more verbose output. Extra occurrences '
                        'of this option, up to 3 total, increase the amount '
                        'of information shown.')
    parser.add_argument('language', metavar='LANGUAGE', type=str, nargs='?',
                        default=None,
                        help='Only show translation progress for language '
                        'LANGUAGE. LANGUAGE should be one of the suffixes of '
                        '../app/src/main/res/values-*')
    args = parser.parse_args()
    return args



# A dictionary with string names and string values,
# e.g. 'action_search' : 'Search'
StringsXML = Dict[str, str]

def parse_strings_xml(filename: str) -> StringsXML:
    """
    Parse a strings.xml into a dictionary.

    :param str filename: The path to the strings.xml file.
    :return: A dictionary with the string names and values as keys and values.
    :rtype: StringsXML
    """
    d = {}
    # Read in the strings.xml data
    xml_root = ET.parse(filename).getroot()
    # Iterate over all translated strings
    for xml_child in xml_root:
        if xml_child.tag == 'string':
            if 'translatable' in xml_child.attrib and xml_child.attrib['translatable'] == "false":
                continue
            if 'name' in xml_child.attrib:
                string_name = xml_child.attrib['name']
                if string_name not in string_blacklist:
                    string_value = xml_child.text
                    d[string_name] = string_value
    return d



# A dictionary containing statistics for a single language's translation
# status. The valid keys are 'translated', 'not_translated' and 'missing' and
# their values are lists of string names that fall into each category. It may
# also contain a key 'filename' with an str value containing the path to the
# strings.xml file it refers to.
SingleLangStats = Dict[str, List[str]]

def compare_strings_xml(eng: StringsXML, other: StringsXML) -> SingleLangStats:
    """
    Compare the translation status of a language with English.

    :param StringsXML eng: The parsed data of the English translation.
    :param StringsXML other: The parsed data of the other language translation.
    :return: A dictionary with the translation statuses and string names as
             keys and values.
    :rtype: SingleLangStats
    """
    result = {'translated': [], 'not_translated': [], 'missing': []}
    # Iterate over all English strings
    for s in eng:
        # Strings are considered translated if they exist in the other
        # strings.xml and their value is different than the English one
        if s in other:
            if other[s] != eng[s]:
                result['translated'].append(s)
            else:
                result['not_translated'].append(s)
        else:
            result['missing'].append(s)
    return result
 


# A dictionary with language names as keys and SingleLangStats as values. It
# contains all the gathered data about all the languages.
LangStats = Dict[str, SingleLangStats]

def csv_print(language_stats: LangStats):
    """
    Print language translation status in CSV format.

    :param LangStats language_stats: The data to be printed.
    """
    print('Language,Filename,Translated,Not Translated,Missing,Completion')
    for lang in language_stats:
        translated = len(language_stats[lang]['translated'])
        not_translated = len(language_stats[lang]['not_translated'])
        missing = len(language_stats[lang]['missing'])
        total = translated + not_translated + missing
        completion = int(100 * translated / total)
        print(lang + ','
                + '"' + language_stats[lang]['filename'] + '"' + ','
                + str(translated) + ','
                + str(not_translated) + ','
                + str(missing) + ','
                + str(completion))

def detailed_print(language_stats: LangStats, verbosity_level: int = 1):
    """
    Print language translation status in human readable format.

    :param LangStats language_stats: The data to be printed.
    :param int verbosity_level: A value of up to 1 will show the number of
                                strings in each translation status
                                ('translated', 'not_translated' and 'missing').
                                A value of 2 will additionally show the names
                                of strings that are 'not_translated' or
                                'missing'. A value of 3 or more with also show
                                the names of strings that are 'translated'.
    """
    for lang in language_stats:
        translated = len(language_stats[lang]['translated'])
        not_translated = len(language_stats[lang]['not_translated'])
        missing = len(language_stats[lang]['missing'])
        total = translated + not_translated + missing
        completion = int(100 * translated / total)
        print('Language: ' + lang)
        print('  File:           ' + language_stats[lang]['filename'])
        print('  Translated:     ' + str(translated)
                + ' (' + str(int(100 * translated / total)) + ' %)')
        if verbosity_level > 2:
            for s in language_stats[lang]['translated']:
                print('      ' + s)
        print('  Not translated: ' + str(not_translated)
                + ' (' + str(int(100 * not_translated / total)) + ' %)')
        if verbosity_level > 1:
            for s in language_stats[lang]['not_translated']:
                print('      ' + s)
        print('  Missing:        ' + str(missing)
                + ' (' + str(int(100 * missing / total)) + ' %)')
        if verbosity_level > 1:
            for s in language_stats[lang]['missing']:
                print('      ' + s)
        print('  Completion:     ' + str(completion) + ' %')



if __name__ == "__main__":
    # Parse command line arguments
    args = parse_arguments()

    # Add the script directory before relative paths to allow calling the
    # script from anywhere
    script_dir = path.dirname(path.realpath(__file__)) + '/'
    english_translation = script_dir + english_translation
    other_translations = glob(script_dir + other_translations)

    # Read in the English translation
    english_strings = parse_strings_xml(english_translation)

    # Iterate over all the other translations
    language_stats = {}
    for filename in other_translations:
        # Get the language name by removing a prefix and a suffix from the
        # filename
        prefix_end_idx = len(script_dir)+len('../app/src/main/res/values-')
        suffix_start_idx = len('/strings.xml')
        language_name = filename[prefix_end_idx:-suffix_start_idx]

        # If a specific language was specified then skip all others
        if args.language and language_name.lower() != args.language.lower():
            continue

        # Read in the other translation
        other_strings = parse_strings_xml(filename)

        # Compare against the English translation
        res = compare_strings_xml(english_strings, other_strings)

        # Add filename info to the results and add the results to the language
        # dictionary
        res['filename'] = filename[len(script_dir):]
        language_stats[language_name] = res

    # Print the results
    if language_stats:
        if args.verbose == 0:
            csv_print(language_stats)
        else:
            detailed_print(language_stats, args.verbose)
    else:
        print('Error: language ' + args.language + ' could not be found')
        exit(1)

