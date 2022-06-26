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


if __name__ == "__main__":

    # 输入影像尺寸
    INPUT_SIZE = 256

    #输入影响地址
    #absolute = sys.argv[1]
    absolute = "E:/Programs/RemoteSensing/RemoteSensing-backend/micro-services/terrian-classification/src/main/resources"
    A_PATH = absolute + '/example/A.jpg'

    # 读取输入影像
    im = cv2.imread(A_PATH)

    #定义训练和验证时使用的数据变换（数据增强、预处理等）
    eval_transforms = T.Compose([
        T.Resize(target_size=256),
        # 验证阶段与训练阶段的数据归一化方式必须相同
        T.Normalize(
            mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5]),
    ])

    # 第一次运行需去掉注释运行，用于生成模型
    # run(
    #     f"python ./../../../../../../PaddleRS/deploy/export/export_model.py \
    #         --model_dir=./dynamic_models/best_model \
    #         --save_dir=./static_models/{INPUT_SIZE}x{INPUT_SIZE} \
    #         --fixed_input_shape=[{INPUT_SIZE},{INPUT_SIZE}]",
    #     shell=True,
    #     check=True
    # )

    modelsPath = absolute + '/static_models'
    predictor = Predictor(f"{modelsPath}/{INPUT_SIZE}x{INPUT_SIZE}", use_gpu=True)

    # 绘制目标框
    with paddle.no_grad():
        im = cv2.resize(im[..., ::-1], (INPUT_SIZE, INPUT_SIZE), interpolation=cv2.INTER_CUBIC)
        pred = predictor.predict(im)

        vis = pred['label_map']
        vis=vis.astype(np.uint8)


    # 存储推理结果
    cv2.imwrite(absolute + './result/result.jpg', vis)
