#!/bin/bash

# this probably isn't safe. Don't do this without first running
# translation-progress.py to understand what data this may remove.

TEMPLATE='reorderTemplate'

reorder(){
    if [ -f "$1.out" ] ; then
        echo "$1.out already exists. Nothing done to it".
    else
        while read LINE ; do
            OUTPUTLINE=""
            if [ ! -z "$LINE" ]; then
                OUTPUTLINE="$(grep "$LINE" "$1")"
            fi
            if [ -z "$OUTPUTLINE" ] ; then
                if [ -z "$LINE" ] ; then
                    OUTPUTLINE=""
                else
                    if [ -z "$(echo $LINE | grep "<string name=" )" ] ; then
                        OUTPUTLINE="$LINE"
                    else
                        # Line exists in template but not in strings.xml.
                        # Use an identifying string so that we identify this
                        # as a line we shouldn't insert (so that we don't end up
                        # with invalid xml)
                        OUTPUTLINE="465761321687"
                    fi
                fi
            fi
            if [ "$OUTPUTLINE" != "465761321687" ] ; then
                echo -e $OUTPUTLINE >> "$1.out"
            fi
        done < $TEMPLATE
    fi
}

for FILE in ../app/src/main/res/values-* ; do
    if [ -f "${FILE}/strings.xml" ] ; then
        reorder "${FILE}/strings.xml"
    fi
done
