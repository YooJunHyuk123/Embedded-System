# Import
from ultralytics import YOLO

# Get model
model = YOLO("yolo11n.pt")

# Train model
model.train(
    data = 'C:/Users/yjh71/Desktop/JH/Student/Grade3/Semester1/Embeddedsystem/Project/DatasetLocal.yaml',
    epochs = 30,
    imgsz = 416,
    batch = 32,
    half = True,
    augment = True
)