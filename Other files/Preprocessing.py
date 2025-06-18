import json
import os

def loop(file_path, output_dir):
    # Load JSON
    with open(file_path, 'r', encoding = 'utf-8') as f:
        data = json.load(f)

    # Get file name
    file_name = data['annotations']['Bbox Annotation']['atchFileName'].replace('.jpg', '.txt')

    # Get width, height
    width, height = map(int, data['meta']['Resolution'].split('x'))

    text_list = []
    for box in data['annotations']['Bbox Annotation']['Box']:
        # Get x, y, w, h
        x = box['x']
        y = box['y']
        w = box['w']
        h = box['h']

        # Nomalize for YOLO
        n_x = (x + w / 2) / width
        n_y = (y + h / 2) / height
        n_w = w / width
        n_h = h / height

        # Get text
        text = f'0 {n_x:.6f} {n_y:.6f} {n_w:.6f} {n_h:.6f}'
        text_list.append(text)

    # Save to file
    with open(os.path.join(output_dir, file_name), 'w') as f:
        f.write('\n'.join(text_list))

def main(input_dir, output_dir):
    for x in os.listdir(input_dir):
        file_path = os.path.join(input_dir, x)
        loop(file_path, output_dir)
        
#main('Dataset/labels/json_train/CarDay', 'Dataset/labels/train/CarDay')
#main('Dataset/labels/json_train/CarNight', 'Dataset/labels/train/CarNight')
#main('Dataset/labels/json_train/SUVDay', 'Dataset/labels/train/SUVDay')
#main('Dataset/labels/json_train/SUVNight', 'Dataset/labels/train/SUVNight')
#main('Dataset/labels/json_val/CarDay', 'Dataset/labels/val/CarDay')
#main('Dataset/labels/json_val/CarNight', 'Dataset/labels/val/CarNight')
#main('Dataset/labels/json_val/SUVDay', 'Dataset/labels/val/SUVDay')
#main('Dataset/labels/json_val/SUVNight', 'Dataset/labels/val/SUVNight')
print('Done')
