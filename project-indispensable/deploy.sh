#!/usr/bin/env bash
curl \
-F "status=2" \
-F "notify=0" \
-F "ipa=@app/build/outputs/apk/release/app-release.apk" \
-H "X-HockeyAppToken: $HockeyAppToken" \
https://rink.hockeyapp.net/api/2/apps/$AndroidAppId/app_versions/upload