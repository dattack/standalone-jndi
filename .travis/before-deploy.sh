#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_e0905f71ae75_key -iv $encrypted_e0905f71ae75_iv -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d
    gpg --fast-import .travis/codesigning.asc
fi
