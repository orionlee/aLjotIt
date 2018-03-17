## First-cut barebone functionality (that I can use)
- [x] basic UI, with send to Keep
- [x] UI works on lock screen (using Quick Tiles)
- [x] General share
- [x] persistence
- [x] App Icon
- [ ] dark theme (and auto theme)
- [ ] Release build related tasks: signing, different version name/code/ app label

## Additional features
- [ ] Lock screen edge case: if the app is in foreground before going to lock screen, put it back to background
- reduce apk / memory footprint (by cutting number of support libs used)
- UI for post lock screen: somehow remind users to send to Keep
    - do we show send / share button on lock screen?
- support Pre Android 7 devices (using lockscreen drawing instead)
- option to show a reminder whenever there is note
- 1st-class support other note app (e.g., OneNote, etc.)
    - replace send to Keep with others
- sticky widget?!