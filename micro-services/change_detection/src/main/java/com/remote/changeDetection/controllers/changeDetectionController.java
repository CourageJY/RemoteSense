package com.remote.changeDetection.controllers;

import com.alibaba.fastjson.JSONObject;
import com.remote.tools.utils.*;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/changeDetection")
@RefreshScope
@Api(value="changeDetection",tags = "changeDetection")
public class changeDetectionController {
    //文件名列表
    public List<String>ls=new ArrayList<>();

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Result<byte[]> test(){
        String path="micro-services/change_detection/src/main/resources";
        String absolute=new File(path).getAbsolutePath();

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

    @RequestMapping(value = "/work",method = RequestMethod.POST)
    public Result<ExtractAns> pic(@RequestParam(value = "pictures") List<MultipartFile> pictures) {
        //获取
        if(pictures.size()<=1){
            return Result.wrapErrorResult("图片数量必须为2");
        }

        //获取随机不重复的文件名
        String a=Radom.getRandomNumber(6,ls);
        String b=Radom.getRandomNumber(6,ls);
        String r=Radom.getRandomNumber(6,ls);

        //保存至本地
        String path="micro-services/change_detection/src/main/resources";
        String absolute=new File(path).getAbsolutePath();
        File dest1 = new File(absolute+ "/input/" + a +".png");
        File dest2 = new File(absolute+ "/input/" + b +".png");

        // 保存文件
        try {
            pictures.get(0).transferTo(dest1);
            pictures.get(1).transferTo(dest2);
        } catch (Exception e) {
            e.printStackTrace();
            //删除文件
            MyFile.DeleteFolder(absolute+ "/input/" + a +".png");
            MyFile.DeleteFolder(absolute+ "/input/" + b +".png");
            ls.remove(a);ls.remove(b);
            return Result.wrapErrorResult("保存图片失败");
        }

        //调用flask端url
        String res=null;
        float rate=0;
        String url="http://127.0.0.1:8300/ChangeDetector";
        Http http=new Http();
        try {
            //传入文件名的参数
            Map<String, Object> map=new HashMap<>();
            map.put("a",a);
            map.put("b",b);
            map.put("r",r);
            //执行请求
            res=http.doGet(url,map);
            System.out.println(res);
            rate= JSONObject.parseObject(res, Ans.class).getRate();
            rate=ExtractAns.GetRate(rate);
        } catch (Exception e) {
            e.printStackTrace();
            //删除文件
            MyFile.DeleteFolder(absolute+ "/input/" + a +".png");
            MyFile.DeleteFolder(absolute+ "/input/" + b +".png");
            MyFile.DeleteFolder(absolute+ "/result/"+ r +".jpg");
            ls.remove(a);ls.remove(b);ls.remove(r);
            return Result.wrapErrorResult("目标url请求失败");
        }

        //读取python运行的结果文件，并以字符流返还至前端
        BufferedImage bufferedImage = null;
        try {
            FileInputStream readPic=new FileInputStream(new File(absolute+"/result/"+r+".jpg"));
            bufferedImage = ImageIO.read(readPic);
            readPic.close();//关闭读入流
        } catch (IOException e) {
            e.printStackTrace();
            return Result.wrapErrorResult("读取结果图片失败");
        }finally {
            //删除文件
            MyFile.DeleteFolder(absolute+ "/input/" + a +".png");
            MyFile.DeleteFolder(absolute+ "/input/" + b +".png");
            MyFile.DeleteFolder(absolute+ "/result/"+ r +".jpg");
            ls.remove(a);ls.remove(b);ls.remove(r);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            assert bufferedImage != null;
            ImageIO.write(Objects.requireNonNull(bufferedImage), "jpg", out);
            return Result.wrapSuccessfulResult(ExtractAns.GetAns(out.toByteArray(),rate));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.wrapErrorResult("失败");
        }
    }
}

//调用并运行python文件
//        String[] arg = new String[] { "python", absolute+"/ChangeDetector.py",absolute};
//        String res=ExeCute.execCmd(arg);
//        float rate=0;
//        if(res==null){
//            return Result.wrapErrorResult("python 脚本执行失败");
//        }
//        else {
//            if(!res.equals("success")){
//                rate= ExtractAns.GetRate(res);
//            }
//        }