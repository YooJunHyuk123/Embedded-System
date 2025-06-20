# 🚗 Embedded System Project - Detect illegal Parking

## 📌 Description
- Illegal parking is a growing issue in urban areas
- This project aims to simplify the detection and reporting of illegal parking by combining mobile and server-side AI technologies
- With the help of an Android application and a Python-based server using YOLO (You Only Look Once), the system can identify illegally parked vehicles from videos and return the processed images back to the user.

## 📂 Project Structure
Embedded-System/

├── Project/              # Android Studio source code

├── Other files/          # Python scripts and additional server resources

└── README.md

## ⚙️ Installation
1. Clone the repository

   ```git clone https://github.com/YooJunHyuk123/Embedded-System.git```
3. Install the required Python libraries

   ```pip install ultralytics opencv-python fastapi uvicorn```
5. Install the required Python libraries
6. Configure server and app URLs
   - Server.py

     ``` # Todo: Change to the current IPv4 address ```

     ``` url = 'http://172.31.52.183:8123' ```
   - Android Studio project files

     ├── MainActivity.kt

     │   ``` // Todo: Change to the current IPv4 address ```

     │   ``` .url("http://172.31.52.183:8123/upload/") ```

     └── FrameListActivity.kt

         ``` // Todo: Change to the current IPv4 address ```

         ``` .url("http://172.31.52.183:8123/frame_list/") ```

## 🚀 Usage
1. Launch the Android app
2. Record a short video of suspected illegal parking
3. The app uploads the video to the server
4. The server uses YOLO to detect vehicles and extract frames with detected objects
5. The detected image is returned to the app and displayed to the user

## ✨ Features
- 📱 Mobile Recording - Capture potential violations via the Android app
- 📤 Video Upload - Automatically uploads videos to the server
- 🎯 Object Detection - YOLO detects vehicles from uploaded videos
- 🖼️ Image Feedback - Server returns image with detection to the app

## 🛠️ Tech Stack
- Android Studio - Mobile app development
- Python - Server-side scripting
- YOLO (Ultralytics) - Real-time object detection
- FastAPI + Uvicorn - Lightweight Python web server

## 🪪 License
- This project is open source
- Feel free to use, modify, and share it!

## 📞 Contact
- Have questions or suggestions?
- 📧 Email: 32212808@dankook.ac.kr

## 🔗 Link
- GitHub: [Embedded-System Repository](https://github.com/YooJunHyuk123/Embedded-System)
- Youtube: [Embedded-System Final Project Presentation](https://www.youtube.com/watch?v=lOunVyQDzFk) // Todo: 추가 예정
- PPT: [Detect illegal Parking - Google Slides](https://docs.google.com/presentation/d/1EHkSGUdTULNgVMy-MlKMbvdZi-SJNLFYwIvmaIo9ekc/edit?slide=id.g36941580fdb_4_12#slide=id.g36941580fdb_4_12)
