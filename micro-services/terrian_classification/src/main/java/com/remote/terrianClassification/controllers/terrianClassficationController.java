package com.remote.terrianClassification.controllers;

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
@RequestMapping("/api/terrianClassfication")
@RefreshScope
@Api(value="terrianClassfication",tags = "terrianClassfication")
public class terrianClassficationController {
    //文件名列表
    public List<String> ls=new ArrayList<>();

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Result<String> test(){
        return Result.wrapSuccessfulResult("It's successful!");
    }

    @RequestMapping(value = "/work",method = RequestMethod.POST)
    public Result<ExtractAns> pic(@RequestParam(value = "picture") MultipartFile picture) {
        //获取随机不重复的文件名
        String a= Radom.getRandomNumber(6,ls);
        String r= Radom.getRandomNumber(6,ls);

        //保存至本地
        String path="micro-services/terrian_classification/src/main/resources";
        String absolute=new File(path).getAbsolutePath();
        File dest1 = new File(absolute+ "/input/" + a +".jpg");

        try {
            picture.transferTo(dest1); // 保存文件
        } catch (Exception e) {
            e.printStackTrace();
            MyFile.DeleteFolder(absolute+ "/input/" + a +".jpg");
            ls.remove(a);
            return Result.wrapErrorResult("保存图片失败");
        }

        //调用url以执行python脚本
        String res=null;
        float rate=0;
        String url="http://127.0.0.1:8300/TerrianClassification";
        Http http=new Http();
        try {
            //传入文件名的参数
            Map<String, Object> map=new HashMap<>();
            map.put("a",a);
            map.put("r",r);
            //执行请求
            res=http.doGet(url,map);
            System.out.println(res);
            rate= JSONObject.parseObject(res, Ans.class).getRate();
            rate=ExtractAns.GetRate(rate);
        } catch (Exception e) {
            e.printStackTrace();
            //删除图片
            MyFile.DeleteFolder(absolute+ "/input/" + a +".jpg");
            MyFile.DeleteFolder(absolute+ "/result/"+ r +".jpg");
            ls.remove(a);ls.remove(r);
            return Result.wrapErrorResult("目标url请求失败");
        }

        //读取python运行文件，并以字符流返还至前端
        BufferedImage bufferedImage = null;
        try {
            FileInputStream readPic=new FileInputStream(new File(absolute+"/result/"+ r +".jpg"));
            bufferedImage = ImageIO.read(readPic);
            readPic.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.wrapErrorResult("读取结果图片失败");
        }finally {
            //删除图片
            MyFile.DeleteFolder(absolute+ "/input/" + a +".jpg");
            MyFile.DeleteFolder(absolute+ "/result/"+ r +".jpg");
            ls.remove(a);ls.remove(r);
        }

        //将字符流返回至前端
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

//        String[] arg = new String[] { "python", absolute+"/TerrianClassification.py",absolute};
//        String res=ExeCute.execCmd(arg);
//        float rate=0;
//        if(res==null) {
//            return Result.wrapErrorResult("目标检测脚本执行失败");
//        }
//        else {
//            if(!res.equals("success")){
//                rate= ExtractAns.GetRate(res);
//            }
//        }
