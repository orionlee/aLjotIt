## First-cut barebone functionality (that I can use)
- [x] basic UI, with send to Keep
- [x] UI works on lock screen (using Quick Tiles)
- [x] General share
- [x] persistence
- [x] App Icon
- [x] themes
    - [x] auto theme
    - [x] UI to choose between light, dark, and auto
    - [x] Configurable auto (rather than hardcoded time interval)
- [x] Release build related tasks: signing, different version name/code/ app label

## Additional features / fixes
- [x] Fix navigation between Main (the note) and Settings, right time pressing back (upper left) does not pop navigation stack. 
It just pushes further the stack.
- [x] Support handling text/links to be shared TO LS Scratch Pad?! (downside: might pollute the share chooser UI)
- [x] Post send to Keep workflow. Implement an option to clear the text.
- [ ] Lock screen UI refinement case
    - if the app is in foreground before going to lock screen, put it back to background
    - On lock screen, once Scratch Pad is brought out, it should be hidden once it is done (while still on lock screen), 
    e.g., upon hitting send button, the UI should be hidden.    
- UI for post lock screen: somehow remind users to send to Keep
    - do we show send / share button on lock screen?
- [ ] The keep-like color scheme maybe confusing to users when both Keep and LS ScratchPad is open, say, in Recent App list
- [ ] (Probably related) Theme change doesn't happen on editor when I hit system back button at the bottom, it does happen if I press the up button on action bar.
- reduce apk / memory footprint (by cutting number of support libs used)
- [ ] Limit the maximum size of a note?!
- Let user customize advanced configuration
   - allow font size / font family be adjustable
   - enable / disable "Add to LS Scratch" (aka share friendly)
   - note maximum size 
- support Pre Android 7 devices (using lockscreen drawing instead)
    - On a 4.1.2 device, the action menu (vertical triple-dot) is missing. Unclear if it is due to Android version or small screen (480x800, unlikely as rotating to landscaping doesn't fix it) .
- option to show a reminder whenever there is note
- 1st-class support other note app (e.g., OneNote, etc.)
    - replace send to Keep with others
- sticky widget?!