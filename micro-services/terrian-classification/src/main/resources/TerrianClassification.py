import sys
# sys.path.append("./../../../../../PaddleRS")
from operator import itemgetter

import paddlers
from paddlers.deploy import Predictor
from paddlers import transforms as T
from paddlers.tasks.utils.visualize import visualize_detection

import paddle
from subprocess import run
import cv2
import numpy as np


def read_rgb(path):
    im = cv2.imread(path)
    im = im[..., ::-1]
    return im

def get_lut():
    lut = np.zeros((256, 3), dtype=np.uint8)
    lut[0] = [255, 0, 0]
    lut[1] = [30, 255, 142]
    lut[2] = [60, 0, 255]
    lut[3] = [255, 222, 0]
    lut[4] = [0, 255, 255]
    return lut


def show_image(im, lut=None):
    if lut is not None:
        im = lut[im]
    else:
        im = cv2.cvtColor(im, cv2.COLOR_BGR2RGB)
    return im

def get_rate(image,n):
    w,h = image.shape
    size = w*h
    count = 0
    for row in image:
        for p in row:
            if p == n:
                count+=1
    return count/size

if __name__ == "__main__":
    # 输入影像尺寸
    INPUT_SIZE = 256

    # 输入影响地址
    absolute =sys.argv[1]
    #absolute="E:/Programs/RemoteSensing/RemoteSensing-backend/micro-services/terrian-classification/src/main/resources"
    A_PATH=absolute+"/example/A.jpg"

    # 读取输入影像
    im = cv2.imread(A_PATH)

    # 第一次运行需去掉注释运行，用于生成模型
    # run(
    #     f"python ./../../../../../PaddleRS/deploy/export/export_model.py \
    #         --model_dir=./dynamic_models/best_model \
    #         --save_dir=./static_models/{INPUT_SIZE}x{INPUT_SIZE} \
    #         --fixed_input_shape=[{INPUT_SIZE},{INPUT_SIZE}]",
    #     shell=True,
    #     check=True
    # )

    modelsPath = absolute+'./static_models'
    predictor = Predictor(f"{modelsPath}/{INPUT_SIZE}x{INPUT_SIZE}", use_gpu=True)

    # 绘制目标框
    with paddle.no_grad():
        # input = predictor.predict(im)['score_map']
        # input = paddle.to_tensor(input)
        # output = paddle.argmax(input,2)
        # res =output.numpy().astype(np.uint8)

        label = predictor.predict(im)['label_map']
        print(get_rate(label,4))
        label = show_image(label, lut=get_lut())

    # 存储推理结果
    cv2.imwrite(absolute+'./result/result.jpg', label)