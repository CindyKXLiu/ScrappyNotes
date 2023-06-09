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

## Features and Demos
### GUI
* Users can create a note, edit note content in a rich text html editor, rename a note and delete a note ([video demo](https://drive.google.com/file/d/1UrbI7FA_jqCs39Vq2dvTtHpGQ-nSVVqJ/view?usp=share_link))
* Users can create a group, add a note to the group, rename the group and delete the group ([video demo](https://drive.google.com/file/d/1bgvOjfgD_mxA7I-hh3oC_N8qsZAFMqkH/view?usp=share_link))
* Users can move and remove notes to and from groups ([video demo](https://drive.google.com/file/d/1sYwxGe6aHbmk9zyFkgN6bII1SRSQULOr/view?usp=sharing))
* Users can search the notes by content and by title ([video demo](https://drive.google.com/file/d/1fjwvD-hxM5la1SCMa00M0Yga9Hjhw66K/view?usp=sharing))
* Users can sort the notes by title, date modified and date created in ascending and descending order ([video demo](https://drive.google.com/file/d/1GyhmvAB-nDY4jytwtFcwm88DVIj_x8fP/view?usp=sharing))
* Users can undo and redo previous actions (new note/group, delete note/group, rename note/group, edit note content, add note to group, move note to group) ([video demo](https://drive.google.com/file/d/1gXW179Y3NW6iuBmKduQc9qvkwky3nVzI/view?usp=sharing))
* Users can choose between 4 different themes for their notes application. The theme they previously chose before exiting the application gets persisted for the next time they open the application ([video demo](https://drive.google.com/file/d/16oTqnqip6WqV2db2obLTmxSRVAywbS6Y/view?usp=share_link))
### Console
* Users can create a new note, rename or open an existing note, delete a note, and list all notes ([video demo](https://drive.google.com/file/d/1oLhTXym_qVIVblunN512YIpH83nYHo6j/view?usp=share_link))
* Users can edit the content of an existing note with the open command ([video demo](https://drive.google.com/file/d/1msC8ykjQzXJqksiCJ7ojoqT2CvM51IGq/view?usp=sharing))
* Users can create groups, and add/remove existing notes to groups ([video demo](https://drive.google.com/file/d/1ZsM2TBDj7LC1-V-KQ-RC1anvCCwNbrAP/view?usp=share_link))
* Users can undo and redo previous actions (new note/group, delete note/group, rename note/group, edit note content, add note to group, move note to group) ([video demo](https://drive.google.com/file/d/1Jw9DrosP7_MfeG8DduZNTISvqwmv4LlR/view?usp=share_link))

### Web Service
* When the user is not connected to the webservice, their data are still persisted to a local database, the data created in one session are stored to this local database and retrieved in new subsequent sessions ([video demo](https://drive.google.com/file/d/1mTKNdS5zCpVgUFKgNRzRYt083BfG3k4X/view?usp=share_link))
* When the user is connected to the webservice, users can save and update to sync changes from across instances ([video demo](https://drive.google.com/file/d/1NbmUcn3jhJUyYCK555TZpw8zdjS3OOQC/view?usp=sharing))
* Both console and GUI will automatically connect to the web service, and can communicate with one another ([video demo](https://drive.google.com/file/d/1BIpusfVSL44OLLQfx4imsPORxbfFKDeL/view?usp=share_link))
## Releases
version 1.0.0: released Feb 17, 2023
* [release-note](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-release-1.0.0.md)
* installers ([Console-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-Console-release-1.0.0.tar), [GUI-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-GUI-release-1.0.0.tar))

version 2.0.0: released March 10, 2023
* [release-note](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-release-2.0.0.md)
* installers ([Console-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-Console-release-2.0.0.tar), [GUI-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-GUI-release-2.0.0.tar))

version 3.0.0: released March 24, 2023
* [release-note](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-release-3.0.0.md)
* installers ([Console-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-Console-release-3.0.0.tar), [GUI-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-GUI-release-3.0.0.tar))

version 4.0.0: released April 10, 2023
*  [release-note](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-release-4.0.0.md)
* installers ([Console-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-Console-release-4.0.0.tar), [Console-macOS](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-Console-release-4.0.0-macOS.zip), [GUI-Windows](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-GUI-release-4.0.0.tar), [GUI-macOS](https://github.com/CindyKXLiu/ScrappyNotes/blob/main/releases/Notes-Application-GUI-release-4.0.0-macOS.zip))

## Other
CSS themes are from the [atlantafx library](https://mkpaz.github.io/atlantafx)
