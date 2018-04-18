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
- [x] Lock screen UI refinement case
    - [x] if the app is in foreground before going to lock screen, put it back to background
    - [x] On lock screen, once Scratch Pad is brought out, and send is pressed:
        - The UI should give user some indication of what's happened (Snackbar indicating send is postponed till unlock).
        - If the user presses send again (still on lock screen), the app won't show up, as
        it is currently in the shared state (cannot be seen by user)
        - Once the screen is unlocked, do the send ASAP (or at least prompt the user to do so).
    - [x] Consider to change lock screen UI from a full screen to a dialog / floating box
        - UX: clearer to users he/she is on lock screen
        - Code: can remove some of the (complicated) logic managing MainActivity's state, e.g., 
         hide MainActivity upon lock screen, UI customization, etc.
- [x] bring up keyboard upon start
- [x] Tweak UI size to avoid overlap with soft keyboard in popular devices.
  - [x] avoid obstructing snackbar messages by hiding soft keyboard.  

- [ ] option to show a reminder whenever there is note
- [ ] Let user customize advanced configuration
   - allow font size / font family be adjustable
   - enable / disable "Add to LS Scratch" (aka share friendly)
   - QS Tile: on normal screen: option to launch Scratch Pad or a new Keep note.
   - note maximum size 

- [x] (BUG, INTERMITTENT]: weird crash at times upon closing apps (somehow broadcast receiver not registered)
  Caused by: java.lang.IllegalArgumentException: Receiver not registered: net.oldev.alsscratchpad.LSScratchPadApp$MainLockScreenReceiver@e4d2aa5
- [x] (BUG) Theme change doesn't happen on editor right away without restart
- [ ] (BUG) on my tablet, lock screen notification does not work after MainActivity is destroyed (user flow: user removes the app from recent app list). 
  - It seems as if after MainActivity unregisters its receiver (MainLockScreenReceiver), LockScreenNotificationReceiver ceases working, even though logcat does not indicate LsnR has been unregiseterd.
  
## For wider usage        
- [ ] support Pre Android 7 devices (using lockscreen drawing / notification instead)
  - [x] Lock Screen Notification (for Android 5 /6)
    - [x] 1st cut
    - [x] misc. edge cases (startup, etc.)
  - [ ] Lock Screen Notification refinement
    - [x] Add warnings if system app notifications is disabled (render lock screen notifications useless)
    - [ ] (MAYBE) Once notification swiped off lock screen, don't show it again until relocked, rather than screen on 
    - [ ] Don't run service if disabled      
  - [ ] Support Android 4 (usable on lock screen), e.g., lockscreen widget 
    - On a 4.1.2 device, the action menu (vertical triple-dot) will disappear if AppCompat is given up.
- [x] Ensure Android 8 devices (in particular notification)

- [ ] 1st-class support other note app (e.g., OneNote, etc.)
    - replace send to Keep with others

## UI Polish for wider usage    
- [x] Rebrand
  - [x] Change the name
  - [x] `NO-OP` Tweak color scheme: The keep-like color scheme maybe confusing to users when both Keep and LS ScratchPad is open, say, in Recent App list
    - gray look weird, yellow is still close to keep
  - [x] Update backend package / class name  
  
- [x] Introduction for initial startup
- [ ] detect Google Keep installation (and error report if it's not installed)

- [ ] Run lint / code inspection
- [ ] Finalize min SDK API 

- [ ] links to rate and feedback, maybe using Google doc forms for feedback

- [ ] Increase lock screen notification clickable areas, not just body text.
- [ ] QSTile on normal screen: for initial use, add a message (better than toast) explaining it launches keep. Long press to launch app itself.

- [] Minor Widget UI tweak
  - the dialog post share (asking to clear it): move it up (rather than center)
  - Toast (after unlock) may need to be moved up too.

- [ ] (MAYBE) reduce apk / memory footprint (by cutting number of support libs used?)
