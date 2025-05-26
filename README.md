# 🧠 Enhanced Mental Health Tips App

A beautiful and interactive **Java Swing** desktop application that helps users manage, favorite, and display daily mental health tips.

---

## 🌟 Features

- ✅ Add new mental health tips
- ✅ Mark tips as **Favorite ⭐**
- ✅ Remove selected tips
- ✅ Undo last removal
- ✅ Sort tips alphabetically
- ✅ Filter to show only favorites
- ✅ Daily tip popup on startup
- ✅ Persist tips using a local `.txt` file
- ✅ User-friendly interface with styled UI

---

## 🖼️ UI Screenshots

> 💡 You can add screenshots here in your GitHub repo under a folder like `/screenshots/` and reference them like below.

| Main Window | Favorite Filter | Add/Remove/Undo | Daily Tip |
|-------------|------------------|------------------|-----------|
| ![Main](screenshots/main.png) | ![Favorites](screenshots/favorites.png) | ![Actions](screenshots/actions.png) | ![Popup](screenshots/popup.png) |

---

## 🛠 Technologies Used

- Java SE 8+
- Swing GUI
- Java Collections (List, Stack)
- File I/O (`java.nio.file.Files`)
- MVC-inspired structure
- Lightweight, no external libraries

---

## 🚀 How to Run

### Prerequisites:
- Java JDK installed (version 8 or higher)
- Any IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code with Java Extension)

### Steps:
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/mental-health-tips-app.git
   cd mental-health-tips-app
Compile and Run: 
javac EnhancedMentalHealthTipsApp.java
java EnhancedMentalHealthTipsApp
📂 Project Structure
mental-health-tips-app/
├── EnhancedMentalHealthTipsApp.java   # Main Java Swing App
├── mental_health_tips.txt             # Saved tips (auto-generated)
├── screenshots/                       # UI screenshots (optional)
└── README.md

