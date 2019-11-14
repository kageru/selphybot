#!/bin/sh
if ./check.sh; then
    ssh lain sudo systemctl restart selphybot.service
fi
