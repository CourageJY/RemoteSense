package com.remote.changeDetection.controllers;

import com.remote.tools.utils.Result;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Objects;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/changeDetection")
@RefreshScope
@Api(value="changeDetection",tags = "changeDetection")
public class changeDetectionController {

    @RequestMapping(value = "/test",method = RequestMethod.POST)
    public Result<String> test(@RequestParam("file") MultipartFile[] file){
        System.out.println(file[0].getOriginalFilename());
        return Result.wrapSuccessfulResult("It's successful!");
    }

    @RequestMapping(value = "/work",method = RequestMethod.POST)
    public Result<byte[]> pic(@RequestParam(value = "pictures") List<MultipartFile> pictures) {
        //获取
        if(pictures.size()<=1){
            return Result.wrapErrorResult("图片数量必须为2");
        }

        //保存至本地
        String path="micro-services/change-detection/src/main/resources";
        String absolute=new File(path).getAbsolutePath();
        File dest1 = new File(absolute+ "/example/" + "A.png");
        File dest2 = new File(absolute+ "/example/" + "B.png");

        try {
            pictures.get(0).transferTo(dest1); // 保存文件
            pictures.get(1).transferTo(dest2);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.wrapErrorResult("保存图片失败");
        }

        //调用并运行python文件
        try {
            String[] arg = new String[] { "python", absolute+"/ChangeDetector.py",absolute};
            Process proc = Runtime.getRuntime().exec(arg);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();//关闭流
            int endFlag = proc.waitFor();//判断process对象是否还在执行
            if (endFlag == 0) {
                System.out.println("The process is ended normally.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
