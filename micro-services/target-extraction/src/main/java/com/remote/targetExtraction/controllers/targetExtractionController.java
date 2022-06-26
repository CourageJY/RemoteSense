package com.remote.targetExtraction.controllers;

import com.remote.tools.utils.ExeCute;
import com.remote.tools.utils.Result;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/targetExtraction")
@RefreshScope
@Api(value="targetExtraction",tags = "targetExtraction")
public class targetExtractionController {
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Result<String> test(){
        return Result.wrapSuccessfulResult("It's successful!");
    }

    @RequestMapping(value = "/work",method = RequestMethod.POST)
    public Result<byte[]> pic(@RequestParam(value = "picture") MultipartFile picture) {
        //保存至本地
        String path="micro-services/target-extraction/src/main/resources";
        String absolute=new File(path).getAbsolutePath();
        File dest1 = new File(absolute+ "/example/" + "A.tiff");

        try {
            picture.transferTo(dest1); // 保存文件
        } catch (Exception e) {
            e.printStackTrace();
            return Result.wrapErrorResult("保存图片失败");
        }

        String[] arg = new String[] { "python", absolute+"/TargetExtraction.py",absolute};
        if(ExeCute.execCmd(arg)==null) {
            return Result.wrapErrorResult("目标检测脚本执行失败");
        }

        //读取python运行文件，并以字符流返还至前端
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new FileInputStream(new File(absolute+"/result/result.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.wrapErrorResult("读取结果图片失败");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            assert bufferedImage != null;
            ImageIO.write(Objects.requireNonNull(bufferedImage), "jpg", out);
            return Result.wrapSuccessfulResult(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.wrapErrorResult("失败");
        }
    }
}
