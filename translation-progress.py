#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: 2020 Sotiris Papatheodorou
# SPDX-License-Identifier: GPL-3.0-or-later

from glob import glob
from os import path
from sys import exit
from typing import Dict, List
import argparse
import xml.etree.ElementTree as ET



english_translation = 'app/src/main/res/values/strings.xml'
other_translations = 'app/src/main/res/values-*/strings.xml'
string_blacklist = [
        'setting_available_simple_string_codes',
        'setting_example_simple_string',
        'dash_clock_status',
        'dash_clock_expanded_title'
]



def parse_arguments():
    parser = argparse.ArgumentParser(description='Show translation progress for Forecastie')
    parser.add_argument('--verbose', '-v', action='count', default=0,
                        help='Produce more verbose output. Extra occurrences '
                        'of this option, up to 3 total, increase the amount '
                        'of information shown.')
    parser.add_argument('language', metavar='LANGUAGE', type=str, nargs='?',
                        default=None,
                        help='Only show translation progress for language '
                        'LANGUAGE. LANGUAGE should be one of the suffixes of '
                        'app/src/main/res/values-*')
    args = parser.parse_args()
    return args



StringsXML = Dict[str, str]

def parse_strings_xml(filename: str) -> StringsXML:
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



SingleLangStats = Dict[str, List[str]]

def compare_strings_xml(eng: StringsXML, other: StringsXML) -> SingleLangStats:
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
 


LangStats = Dict[str, SingleLangStats]

def csv_print(language_stats: LangStats):
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
        prefix_end_idx = len(script_dir)+len('app/src/main/res/values-')
        suffix_start_idx = len('/strings.xml')
        language_name = filename[prefix_end_idx:-suffix_start_idx]

        # If a language was specified skip all others
        if args.language and language_name.lower() != args.language.lower():
            continue

        # Read in the other translation
        other_strings = parse_strings_xml(filename)

        # Compare agains the English translation
        res = compare_strings_xml(english_strings, other_strings)

        # Add filename info and add the results to the language dictionary
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

