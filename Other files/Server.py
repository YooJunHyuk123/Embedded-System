# Standard library
from datetime import datetime # Features related to data and time
import os # Interactions with Operating Systems
import shutil # Features to copy, delete, and move directories and files

# External library
import cv2 # Processing images & videos
from ultralytics import YOLO # Recall deep learning models to infer
from fastapi import FastAPI, File, UploadFile # Python framework for creating web API servers
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import uvicorn # ASGI server for running FastAPI server

# Todo: Change to the current IPv4 address
url = 'http://172.31.52.183:8123'

# Setting up an absolute path-based directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
UPLOAD_DIR = os.path.join(BASE_DIR, 'uploads')
FRAME_DIR = os.path.join(BASE_DIR, 'frames')
os.makedirs(UPLOAD_DIR, exist_ok = True)
os.makedirs(FRAME_DIR, exist_ok = True)

# Load YOLO model
try:
    model = YOLO(os.path.join(BASE_DIR, 'best.pt'))
except Exception as e:
    print(f'[ERROR] Load YOLO model failed: {e}')
    model = None

app = FastAPI() # Create FastAPI server object
app.mount('/frames', StaticFiles(directory = 'frames'), name = 'frames') # Make image files in folders accessible from the server

@app.post('/upload/') # Run on POST request
async def upload_video(file: UploadFile = File(...)): # Asynchronous execution
    try:
        # Save the video
        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        filename = f'{timestamp}_{file.filename}'
        video_path = os.path.join(UPLOAD_DIR, filename)
        with open(video_path, 'wb') as buffer:
            shutil.copyfileobj(file.file, buffer)

        print(f'[INFO] Video saved: {video_path}')

        # Open the video
        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            print(f'[ERROR] Open the video failed: {video_path}')
            return JSONResponse(content = {'status': 'error', 'detail': 'Open the video failed:'}, status_code = 500)

        # Load YOLO model
        if model is None:
            print('[ERROR] Load YOLO model failed')
            return JSONResponse(content = {'status': 'error', 'detail': 'Load YOLO model failed'}, status_code = 500)

        # Read the frame & apply YOLO
        frame_count = 0
        saved_frame_count = 0
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            frame_count += 1

            # YOLO every 15 frames & If there is a dectection result than save the image
            if frame_count % 15 == 0:
                results = model.predict(frame)
                if results and len(results) > 0 and hasattr(results[0], 'boxes') and results[0].boxes is not None:
                    # Draw bounding box on result image
                    frame_name = os.path.join(FRAME_DIR, f'frame_{timestamp}_{saved_frame_count}.jpg')

                    # Save the image
                    cv2.imwrite(frame_name, frame)
                    saved_frame_count += 1

        cap.release()
        print(f'[INFO] Number of detected: {saved_frame_count}')
        return JSONResponse(content = {'status': 'success', 'frames_saved': saved_frame_count})

    except Exception as e:
        print(f'[ERROR] {e}')
        return JSONResponse(content = {'status': 'error', 'detail': str(e)}, status_code = 500)

@app.get('/frame_list/') # Run on GET request
async def list_frames(): # Asynchronous execution
    
    # Returns the list of image files in a folder as a URL list
    files = os.listdir('frames')
    files.sort()
    urls = [f'{url}/frames/{name}' for name in files]
    return {'frames': urls}

if __name__ == '__main__':
    uvicorn.run(app, host = '0.0.0.0', port = 8123)
