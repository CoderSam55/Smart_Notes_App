# 📚 SmartNotes App

**SmartNotes** is a digital note-management application designed to help students create, organize, and access their study notes in one place.

The application allows students to create notes for different subjects, add images and study material, and easily access or share their notes digitally.

## 🚀 Features

### 📝 Note Management

* Create and save study notes
* Edit existing notes
* Delete unwanted notes
* Organize notes by subject
* View notes in an organized interface

### 📷 Image Support

* Add images to notes
* Store study-related images
* Use diagrams and visual learning material inside notes

### 📚 Subject Organization

* Create notes according to subjects
* Easily find subject-specific study material
* Keep academic content organized in one application

### 📄 PDF Support

* Convert notes into PDF format
* Download notes for offline access
* Share generated study material

### 🔗 Sharing

* Share notes with other students
* Share notes using generated links
* Access shared study material conveniently

## 🛠️ Technologies Used

* **Java**
* **Android Studio**
* **XML**
* **Material Design**
* **ViewBinding**
* **Supabase**
* **PostgreSQL**
* **Supabase Storage**
* **Supabase Authentication**

## 🏗️ Application Flow

```text id="j3e1u8"
Login / Register
       │
       ▼
   Dashboard
       │
 ┌─────┼─────────┐
 ▼     ▼         ▼
Notes  Subjects  Profile
 │
 ▼
Create Note
 │
 ├── Add Text
 ├── Add Images
 └── Select Subject
       │
       ▼
     Save
       │
       ▼
   View / Edit
       │
 ├── Generate PDF
 └── Share Note
```

## 🔐 Authentication

The application provides user authentication so that users can securely access their personal notes.

```text id="8c5rhv"
Login / Register
       │
       ▼
   User Account
       │
       ▼
   Personal Notes
```

## ☁️ Backend & Storage

Supabase is used as the backend platform.

### Database

PostgreSQL is used to manage:

* User information
* Notes
* Subjects
* Note metadata
* Shared notes

### Storage

Supabase Storage can be used to store:

* Images
* Uploaded study material
* Generated documents

## 📱 Main Modules

### 👤 User Module

* Registration and login
* User profile
* Personal notes

### 📖 Notes Module

* Create notes
* Edit notes
* Delete notes
* View notes
* Organize notes

### 📷 Media Module

* Add images
* Store study material
* Display images inside notes

### 📄 PDF Module

* Generate PDFs
* Download study material
* Share PDFs

### 🔗 Sharing Module

* Share notes
* Access shared study material
* Generate shareable links

## 🎯 Project Objective

The main objective of SmartNotes is to provide students with a centralized digital platform for managing their academic notes.

Instead of maintaining notes across notebooks, images, and different applications, students can organize their study material in a single application.

## 🔮 Future Improvements

* AI-powered note summarization
* AI-generated flashcards
* Voice-to-text notes
* OCR for handwritten notes
* Smart note recommendations
* Offline note synchronization
* Advanced search
* Note reminders and notifications

## 📸 Screenshots

Add application screenshots here.

Example:

```text id="4c9u7m"
screenshots/
├── login.png
├── dashboard.png
├── notes.png
├── create-note.png
└── profile.png
```

## 👨‍💻 Author

**CoderSam55**

GitHub: `https://github.com/codersam55`

---

⭐ If you find this project useful, consider giving it a star!
