# CS346 - Scrappy Notes

## Goal
A note-taking application which encourages users to store their fleeing thoughts and ideas in a chaotic yet organized way.
It includes common notes features such as adding, editing, deleting notes, as well as ways to organize/search them.

Our application includes both a GUI and a simple console interface.

## Team members
Cindy Liu: kxliu@uwaterloo.ca

Amanda Tang: a54tang@uwaterloo.ca

Joanne Wu: j545wu1@uwaterloo.ca

Reyna Lu (Rerenah): r55lu@uwaterloo.ca


## Quick-start

To use the webservice, please run the following commands in terminal before starting the app:

```
docker pull rerenah/notes-app-webservice
docker run -p 8080:8080 --mount type=volume,source=data-storage,destination=/data rerenah/notes-app-webservice
```

To run the application:
* Select the latest available version of either the GUI, or Console release.
* Download the available tar files and extract them to the desired location.
* Open the bin folder and run the batch file

## Screenshots/videos
Optional, but often helpful to have a screenshot or demo-video for new users.

## Releases
version 1.0.0: released Feb 17, 2023
* [release-note](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-release-1.0.0.md)
* installers ([Console-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-Console-release-1.0.0.tar), [GUI-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-1.0.0.tar))

version 2.0.0: released March 10, 2023
* [release-notes](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-release-2.0.0.md)
* installers ([Console-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-Console-release-2.0.0.tar), [GUI-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-2.0.0.tar))

version 3.0.0: released March 24, 2023
* [release-notes](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-release-3.0.0.md)
* installers ([Console-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-Console-release-3.0.0.tar), [GUI-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-3.0.0.tar))

version 4.0.0: released April 10, 2023
*  [release-notes](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-release-4.0.0.md)
* installers ([Console-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-Console-release-4.0.0.tar), [Console-macOS](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-4.0.0-macOS.zip), [GUI-Windows](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-4.0.0.tar), [GUI-macOS](https://git.uwaterloo.ca/a54tang/cs346/-/blob/main/releases/Notes-Application-GUI-release-4.0.0-macOS.zip))

## Other
CSS themes are from the [atlantafx library](https://mkpaz.github.io/atlantafx)