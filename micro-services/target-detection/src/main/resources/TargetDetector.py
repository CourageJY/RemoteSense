import sys
#sys.path.append("./../../../../../PaddleRS")

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
    im = im[...,::-1]
    return im

if __name__ == "__main__":

    # 输入影像尺寸
    INPUT_SIZE = 608

    # 输入影响地址
    absolute = sys.argv[1]
    #absolute="E:/Programs/RemoteSensing/RemoteSensing-backend/micro-services/target-detection/src/main/resources"
    A_PATH = absolute+'/example/A.jpg'


    # 读取输入影像
    im = cv2.imread(A_PATH) 


    eval_transforms = T.Compose([
        # 使用双三次插值将输入影像缩放到固定大小
        T.Resize(
            target_size=INPUT_SIZE, interp='CUBIC'
        ),
        # 与训练阶段的归一化方式必须相同
        T.Normalize(
            mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]
        )
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
        vis_res = []

        im = cv2.resize(im[...,::-1], (INPUT_SIZE, INPUT_SIZE), interpolation=cv2.INTER_CUBIC)
        pred = predictor.predict(im, eval_transforms)

        vis = im
        # 用绿色画出预测目标框
        if len(pred) > 0:
            vis = visualize_detection(
                np.array(vis), pred,
                color=np.asarray([[0,255,0]], dtype=np.uint8),
                threshold=0.2, save_dir=None
            )


    # 存储推理结果
    cv2.imwrite(absolute+'./result/result.jpg', vis)
