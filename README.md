# kagebot – where the code is better than the name
This bot is a replacement for [my old one](https://git.kageru.moe/kageru/discord-selphybot) with a very simple premise:
As much as possible should be configurable in a human-readable way.
This will allow anyone to modify the config to host their own instance tailored to their own needs,
and it allows server moderators to make changes without any coding experience. Even at runtime.
I try to maintain a comprehensive default configuration as part of the repository
because the past has taught me that I’m not good at updating readmes.

## A few months after
The bot has become somewhat specialized at this point,
  but I think it should still be generally reusable if a similar use case arises.
The implementation has kind of deteriorated into a playground for me
  (adding arrow-kt and just generally trying out FP stuff)[1],
  but it’s been running and moderating a 1000+ user server for over a year
  with relatively little maintenance.

[1]: While arrow is great, adding it to a project after the fact leads to a very weird combination of FP and non-FP constructs. Would not recommend in production.
