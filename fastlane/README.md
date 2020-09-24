fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
## Android
### android beta
```
fastlane android beta
```
Deploy a new beta version to the Google Play
### android deploy_staged
```
fastlane android deploy_staged
```
Deploy a new staged release version to the Google Play
### android deploy
```
fastlane android deploy
```
Deploy a new version to the Google Play
### android changelog
```
fastlane android changelog
```
Generates a changelog from git commits and puts it in the clipboard
### android tag
```
fastlane android tag
```
Creates and pushes a release tag

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
