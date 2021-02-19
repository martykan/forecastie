#!/usr/bin/env python3
#
# Tool that recreates the English language file structure
# for another language without loosing currently used translations.
# The updated files are created in "./tools/new_strings".
# The updated file will loose any comment outside xml fields (so do
# not forget to copy them back).
#
# This script copies a lot from translation-progress.py to avoid
# having to import anything.
#

from glob import glob
from os import path
from typing import Dict, List
import argparse
import shutil
import xml.etree.ElementTree as ET

# These paths are relative to the script and should be changed accordingly if
# the script is moved
_english_dir = '../app/src/main/res/values'
_other_lang_dirs = '../app/src/main/res/values-{language}'

# This is where the updated files will be created
_new_dir = "./new_strings"

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

# A dictionary from string names to list of string values and filenames,
# e.g. 'action_search' : ('Search', 'strings_main_graphs_map_about.xml')
StringsXML = Dict[str, List[str]]

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


def selected_language_dir() -> List[str]:
    """
    Find the directory containing the string XML files for any language other
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
                        d[string_name] = [string_value, path.basename(filename)]
    return d


def update_strings_xml(original_strings: StringsXML, other: StringsXML) -> StringsXML:
    """
    Update the English translation with the selected language.

    :param original_strings: StringsXML eng: The parsed data of the English translation.
    :param other: StringsXML other: The parsed data of the other language translation.
    :return: An updated dictionary with the english string names and language values as keys and values.
    :rtype: StringsXML
    """
    for s in original_strings:
        if s in other:
            original_strings[s][0] = other[s][0]
    return original_strings


def write_new_strings(new_strings: StringsXML):
    files_list = list(set([new_strings[f][1] for f in new_strings]))
    for filename in files_list:

        # copy "_english_dir" to "./new_strings"
        origin = f"{_english_dir}/{filename}"
        destination = f"{_new_dir}/{filename}"
        shutil.copy2(origin, destination)

        # Read in the strings.xml data
        parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=True))
        xml_tree = ET.parse(destination, parser=parser)
        xml_root = xml_tree.getroot()
        # Iterate over all translated strings
        for xml_child in xml_root:
            if xml_child.tag == 'string':
                if 'name' in xml_child.attrib:
                    string_name = xml_child.attrib['name']
                    string_value = new_strings[string_name][0]
                    if string_name not in _ignored_strings:
                        xml_child.text = string_value
        xml_tree.write(destination, encoding="utf-8")
        print(f"{destination} created and updated with available translations.")
    print("Don't forget to add back the top comments present in the English version.")


def parse_arguments():
    """
    Parse command line arguments.

    :return: A Namespace containing the arguments and their values.
    :rtype: argparse.Namespace
    """
    parser = argparse.ArgumentParser(description='Create an updated files structure for a language for Forecastie')
    parser.add_argument('language', metavar='LANGUAGE', type=str,
                        default=None,
                        help='Selected language '
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
    dirname = _other_lang_dirs.format(language=args.language)
    print(dirname)

    # Read in the translation in this language
    other_strings = parse_strings_xml(string_xml_files(dirname))

    updated_strings = update_strings_xml(english_strings, other_strings)
    write_new_strings(updated_strings)
