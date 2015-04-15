
## Documentation for creating a new release

1. Finish last commit before releasing
2. Edit `app/build.gradle`
  * Bump `versionCode` +1
  * Edit `versionName` to appropriate value
3. Commit to git: 'Version bump: vx.x.x'
4. Tag in git: `git tag -a vx.x.x`
5. Checkout tag in git
6. Create signed APK in Android Studio
7. Upload to Play Developer console
8. Create next milestone