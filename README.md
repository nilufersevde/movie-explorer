# 🎬 Movie Explorer

Movie Explorer is an Android app that allows users to search for movies, view what's currently playing in theaters, and manage personal favorites and watched lists.

---

## 🚀 Features

- 🔍 **Search movies** by name using the TMDb API
- 🆕 **Now Playing** section – view movies currently in theaters
- ❤️ Mark movies as **Favorites**
- ✅ Track movies as **Watched**
- 🔐 **User authentication** with Firebase
- 🗃️ **Local caching** with Room and CSV import fallback

---

## 🧰 Tech Stack

- **Kotlin**
- **Android Jetpack**: ViewModel, LiveData, Room, Paging 3
- **Firebase**: Authentication & Firestore
- **TMDb API** – for live movie data
- **Glide** – image loading
- **MVVM Architecture**

---

## 🖼️ Screenshots

_Add your app screenshots here for visual preview._

---

## 🛠️ Setup Instructions

1. Clone the repo:

   ```bash
   git clone https://github.com/nilufersevde/movie-explorer.git
   ```

2. Open the project in Android Studio

3. Add your TMDb API key in `local.properties`:

   ```properties
   TMDB_API_KEY=your_api_key_here
   ```

4. Connect your Firebase project (or comment out Firebase code for local testing)

5. Run the app on an emulator or physical device

---

## 📂 Notes

- The original 500MB+ movie CSV file is excluded from this repo due to GitHub’s file size limit.
- If you'd like the CSV file to test Room DB offline features, contact me privately or request access via email.
- Favorites and watched lists are stored per user in Firestore.

---

## 📄 License

MIT License. Feel free to use and modify for educational purposes.

---

## 👤 Author

**Nilüfer Sevde Özdemir**  
Built as part of the Mobile Programming course at Akdeniz University.


