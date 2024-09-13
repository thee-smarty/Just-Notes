# [Just Notes](https://play.google.com/store/apps/details?id=com.theesmarty.justnotes)

JustNotes is a simple notes app that allows users to securely create, edit, view, and delete notes. With Google Sign-In and cloud storage, users can access their notes from any device. The app features a clean and easy-to-use interface, making it ideal for quick note-taking.

## Features

### Authentication

- Secure login through Google Sign-In using Firebase Authentication.
- User-specific notes are securely stored and accessed using Firebase's backend services.
- Quick and easy login experience.

### Cross-Device Access

- All notes are stored in Firebase Firestore, ensuring that they are tied to the user’s account.
- Users can log in on any Android device with their Google account and instantly access their notes.
- Any changes or updates made to notes on one device are automatically synced to all devices.

### User-Friendly UI

- Clean and minimalistic interface for a distraction-free experience.
- Easy navigation with essential buttons for adding and viewing notes.
- User-friendly design focused on simplicity and ease of use.

### Simple CRUD Operations

- Create: Easily add new notes with a title and content.
- Read: View the list of notes you have created.
- Update: Edit any existing note to modify its title or content.
- Delete: Remove unwanted notes from your list.

## Technology Stack

- **IDE**: Android Studio
- **Language**: Java
- **Authentication**: Firebase OAuth
- **Database**: Firestore
