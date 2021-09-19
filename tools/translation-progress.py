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
# - Translated:     The string is present in both the English and the other
#                   language translations and its value in the other language
#                   translation is different than in English.
# - Not translated: The string is present in both the English and the other
#                   language translations but its value in the other language
#                   translation is the same as in English.
# - Missing:        The string is present only in the English translation and
#                   not in the other language translation.
# - Misplaced:      The string is located in the wrong file. Both translated
#                   and not translated strings are considered.
#
# Strings with the translatable="false" attribute or whose names are in the
# _ignored_strings are ignored.
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
#        - Misplaced      The number of misplaced strings (only if the
#                         respective option is provided).
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
from typing import Dict, List, Tuple
import argparse
import xml.etree.ElementTree as ET



# These paths are relative to the script and should be changed accordingly if
# the script is moved
_english_dir = '../app/src/main/res/values'
_other_lang_dirs = '../app/src/main/res/values-*'

# The glob pattern used to find string XML files
_string_xml_glob = 'strings*.xml'

# Directories to ignore when searching for other languages
_ignored_lang_dirs = [
        'values-night',
        'values-v21',
        'values-v27',
]

# String XML files to ignore when checking translated strings
_ignored_string_xml = [
        'strings_not_translated.xml',
]

# String names to ignore when checking translated strings
_ignored_strings = [
    'setting_available_simple_string_codes',
    'setting_example_simple_string',
]



# A dictionary from string names to tuples of string values and filenames,
# e.g. 'action_search' : ('Search', 'strings_main_graphs_map_about.xml')
StringsXML = Dict[str, Tuple[str, str]]

# A dictionary containing statistics for a single language's translation
# status. The valid keys are 'translated', 'not_translated', 'missing' and
# 'misplaced' and their values are lists of string names that fall into each
# category. It may also contain a key 'dirname' with an str value containing
# the path to the strings.xml file it refers to.
SingleLangStats = Dict[str, List[str]]

# A dictionary with language names as keys and SingleLangStats as values. It
# contains all the gathered data about all the languages.
LangStats = Dict[str, SingleLangStats]



def script_dir() -> str:
    """
    Return the path to the directory containing this file.

    :return: The full path to the directory containing this file with a
             trailing slash.
    :rtype: str
    """
    return path.dirname(path.realpath(__file__)) + '/'



def other_language_dirs() -> List[str]:
    """
    Find the directories containing the string XML files for languages other
    than English.

    :return: A list of paths to the directories containing the string XML files.
    :rtype: List[str]
    """
    dirnames = [x.rstrip('/') for x in glob(script_dir() + _other_lang_dirs)]
    return sorted([x for x in dirnames if path.basename(x) not in _ignored_lang_dirs])



def string_xml_files(dirname: str) -> List[str]:
    """
    Find the language string XML files contained in dirname.

    :param str filename: The directory containing the string XML files.
    :return: A list of paths to the XML files.
    :rtype: List[str]
    """
    filenames = glob(dirname + '/' + _string_xml_glob)
    return sorted([x for x in filenames if path.basename(x) not in _ignored_string_xml])



def english_xml_files() -> List[str]:
    """
    Find the English string XML files.

    :return: A list of paths to the English XML files.
    :rtype: List[str]
    """
    return string_xml_files(script_dir() + _english_dir)



def get_lang_name(dirname: str) -> str:
    """
    Return the language code given the directory containing its string XML files.

    :param str dirname: The directory containing the string XML files.
    :return: The language code extracted from dirname.
    :rtype: str
    """
    return path.basename(dirname.rstrip('/'))[7:]



def parse_strings_xml(filenames: List[str]) -> StringsXML:
    """
    Parse the supplied strings XML files into a single dictionary.

    :param List[str] filenames: The paths to the strings XML files.
    :return: A dictionary with the string names and values as keys and values.
    :rtype: StringsXML
    """
    d = {}
    for filename in filenames:
        # Read in the strings.xml data
        xml_root = ET.parse(filename).getroot()
        # Iterate over all translated strings
        for xml_child in xml_root:
            if xml_child.tag == 'string':
                if 'translatable' in xml_child.attrib \
                        and xml_child.attrib['translatable'] == "false":
                    continue
                if 'name' in xml_child.attrib:
                    string_name = xml_child.attrib['name']
                    if string_name not in _ignored_strings:
                        string_value = xml_child.text
                        d[string_name] = (string_value, path.basename(filename))
    return d



def compare_strings_xml(eng: StringsXML, other: StringsXML) -> SingleLangStats:
    """
    Compare the translation status of a language with English.

    :param StringsXML eng: The parsed data of the English translation.
    :param StringsXML other: The parsed data of the other language translation.
    :return: A dictionary with the translation statuses and string names as
             keys and values.
    :rtype: SingleLangStats
    """
    result = {'translated': [], 'not_translated': [], 'missing': [], 'misplaced': []}
    # Iterate over all English strings
    for s in eng:
        # Strings are considered translated if they exist in the other
        # strings.xml and their value is different than the English one
        if s in other:
            # Test if the value of the string differs from the English one
            if other[s][0] != eng[s][0]:
                result['translated'].append(s)
            else:
                result['not_translated'].append(s)
            # Test if the file the string was in differs from the English one
            if other[s][1] != eng[s][1]:
                result['misplaced'].append(s)
        else:
            result['missing'].append(s)
    return result
 


def csv_print(language_stats: LangStats, show_misplaced: bool = False):
    """
    Print language translation status in CSV format.

    :param LangStats language_stats: The data to be printed.
    :param bool show_misplaced: Show data for misplaced strings.
    """
    header = 'Language,Filename,Translated,Not Translated,Missing'
    if show_misplaced:
        header += ',Misplaced'
    header += ',Completion'
    print(header)
    for lang in language_stats:
        translated = len(language_stats[lang]['translated'])
        not_translated = len(language_stats[lang]['not_translated'])
        missing = len(language_stats[lang]['missing'])
        misplaced = len(language_stats[lang]['misplaced'])
        total = translated + not_translated + missing
        completion = int(100 * translated / total)
        line = ','.join([lang, '"' + language_stats[lang]['dirname'] + '"',
                str(translated), str(not_translated), str(missing)])
        if show_misplaced:
            line += ',' + str(misplaced)
        line += ',' + str(completion)
        print(line)



def detailed_print(language_stats: LangStats, verbosity_level: int = 1, show_misplaced: bool = False):
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
    :param bool show_misplaced: Show data for misplaced strings.
    """
    num_pc_fmt = '{:3d} ({:3d}%)'
    for lang in language_stats:
        translated = len(language_stats[lang]['translated'])
        not_translated = len(language_stats[lang]['not_translated'])
        missing = len(language_stats[lang]['missing'])
        misplaced = len(language_stats[lang]['misplaced'])
        total = translated + not_translated + missing
        completion = int(100 * translated / total)
        not_translated_pc = int(100 * not_translated / total)
        missing_pc = int(100 * missing / total)
        misplaced_pc = int(100 * misplaced / (translated + not_translated))
        print('Language: ' + lang)
        print('  File:           ' + language_stats[lang]['dirname'])
        print(('  Translated:     ' + num_pc_fmt).format(translated, completion))
        if verbosity_level > 2:
            for s in language_stats[lang]['translated']:
                print('      ' + s)
        print(('  Not translated: ' + num_pc_fmt).format(not_translated, not_translated_pc))
        if verbosity_level > 1:
            for s in language_stats[lang]['not_translated']:
                print('      ' + s)
        print(('  Missing:        ' + num_pc_fmt).format(missing, missing_pc))
        if verbosity_level > 1:
            for s in language_stats[lang]['missing']:
                print('      ' + s)
        print(('  Misplaced:      ' + num_pc_fmt).format(misplaced, misplaced_pc))
        if verbosity_level > 1:
            for s in language_stats[lang]['misplaced']:
                print('      ' + s)
        print('  Completion:     {:3d}%'.format(completion))



def parse_arguments():
    """
    Parse command line arguments.

    :return: A Namespace containing the arguments and their values.
    :rtype: argparse.Namespace
    """
    parser = argparse.ArgumentParser(description='Show translation progress for Forecastie')
    parser.add_argument('--misplaced', '-m', action='store_true',
                        help='Gather and show statistics about misplaced'
                        'strings (strings located in the wrong file).')
    parser.add_argument('--verbose', '-v', action='count', default=0,
                        help='Produce more verbose output. Extra occurrences '
                        'of this option, up to 3 total, increase the amount '
                        'of information shown.')
    parser.add_argument('language', metavar='LANGUAGE', type=str, nargs='?',
                        default=None,
                        help='Only show translation progress for language '
                        'LANGUAGE. LANGUAGE should be one of the suffixes of '
                        + _other_lang_dirs)
    args = parser.parse_args()
    return args



if __name__ == "__main__":
    # Parse command line arguments
    args = parse_arguments()

    # Read in the English translation
    english_strings = parse_strings_xml(english_xml_files())

    # Iterate over all the other translations
    language_stats = {}
    for dirname in other_language_dirs():
        # Get the language name from the dirname suffix
        language_name = get_lang_name(dirname)

        # If a specific language was specified then skip all others
        if args.language and language_name.lower() != args.language.lower():
            continue

        # Read in the translation in this language
        other_strings = parse_strings_xml(string_xml_files(dirname))

        # Compare against the English translation
        res = compare_strings_xml(english_strings, other_strings)

        # Add dirname info to the results and add the results to the language
        # dictionary
        res['dirname'] = path.basename(dirname)
        language_stats[language_name] = res

    # Print the results
    if language_stats:
        if args.verbose == 0:
            csv_print(language_stats, args.misplaced)
        else:
            detailed_print(language_stats, args.verbose, args.misplaced)
    else:
        print('Error: language ' + args.language + ' could not be found')
        exit(1)

