# CS346 - Scrappy Notes

## Goal
A note-taking application which encourages users to store their fleeing thoughts and ideas in a chaotic yet organized way.
It includes common notes features such as adding, editing, deleting notes, as well as ways to organize/search them. 
It also includes some additional features such as themes, image support, etc. 
Our application includes both a GUI and a console interface.

## Team members
Cindy Liu: kxliu@uwaterloo.ca

Amanda Tang: a54tang@uwaterloo.ca

Joanne Wu: j525wu1@uwaterloo.ca

Reyna Lu (Rerenah): r55lu@uwaterloo.ca


## Quick-start
### GUI
#### Windows
1. Unzip the GUI tar file.
2. Go to the ```/bin``` folder and run the batch file. It may take a few seconds to start.

The GUI can also be accessed via console app from the command line.
1. Unzip the Console zip file.
2. Open up terminal and navigate to the ```/bin``` folder.
3. Run the console with ```--launchGUI``` argument (```console --launchGUI```). 

#### MacOS
1. Unzip the GUI zip file.
2. Go to the ```/bin``` folder and click on the executable.

The GUI can also be accessed via console app from the command line.
1. Unzip the Console zip file.
2. Open up terminal and navigate to the ```/bin``` folder.
3. Run the console with ```--launchGUI``` argument (```./console --launchGUI```). 

### Console
#### Windows
1. Unzip the Console tar file.
2. Go to the ```/bin``` folder and run the batch file.
#### MacOS
1. Unzip the Console zip file. 
2. Open up terminal and navigate to the ```/bin``` folder.
3. Run the console with no arguments (```./console```).

### Web Service
To use the webservice, please run the following commands in terminal before starting the app:

```
docker pull rerenah/notes-app-webservice
docker run -p 8080:8080 --mount type=volume,source=data-storage,destination=/data rerenah/notes-app-webservice
```

## Screenshots/videos
### GUI
### Console
* Users can create a new note, rename or open an existing note, delete a note, and list all notes from console ([video demo](https://drive.google.com/file/d/1oLhTXym_qVIVblunN512YIpH83nYHo6j/view?usp=share_link))
* Users can create groups, and add/remove existing notes to groups from console ([video demo](https://drive.google.com/file/d/1ZsM2TBDj7LC1-V-KQ-RC1anvCCwNbrAP/view?usp=share_link ))
* Users can undo and redo previous actions from console ([video demo](https://drive.google.com/file/d/1Jw9DrosP7_MfeG8DduZNTISvqwmv4LlR/view?usp=share_link ))

### Web Service


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
