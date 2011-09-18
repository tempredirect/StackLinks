#!/bin/sh

STACKLINKS_HOME=/home/ubuntu/stacklinks
echo "starting download"

/usr/bin/transmission-cli $1 --finish $STACKLINKS_HOME/scripts/process_datadump.rb



