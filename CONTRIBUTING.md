# Contributing
Thank you for wanting to contribute to this project! There are just a few basic rules that PRs need to follow.

## No translator/AI translations
Please do not try to PR translation files created using Google Translate/DeepL/AI/etc. Due to the low quality and missing context, these translations just cause more work for everyone else to clean up later.

## No generated/automated/redundant PRs
Some examples of this:
- Throwing textures into a tool to shave a few KB from the file is not a productive change. It doesn't change anything for the better, and could be repeated by anyone with a tiny bit more loss to farm contributions.
- Pure text/code formatting changes. The formatting is done by Gradle, if something can be improved, instead update the auto formatting.
- The "English Upside-down" language file can be generated from the normal English file. So a build step that generates this file makes way more sense, than PRing and maintaining such a trivial file.

## No content falling under Modrinths ``Usage of Generative "AI"`` rule
All PRs must not fall under the [Disclosure of AI generated content](https://modrinth.com/legal/rules#disclosure-of-ai-generated-content) rule by Modrinth. That means:
- No substantial portion of the code is a product of AI output.
- No assets that are primarily or entirely a product of AI output.
- No commits signed off by agents like Codex/Copilot, there needs to be a person responsible for the changes, not AI.

Also please [Don't be a meat proxy](https://gruhn.me/blog/2026-08-03/).

## Run a build before the PR
Simply run ``.\gradlew clean build`` before creating the pull request to run the formatter and verify that no other Minecraft versions are broken from the changes.
