#!/bin/sh
if gradle test; then
    gradle shadowjar
    scp build/libs/moe.kageru.kagebot-0.1-all.jar lain:/home/selphybot/bot.jar
    ssh lain sudo systemctl restart selphybot.service
fi
