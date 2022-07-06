package com.remote.targetDetection.controllers;

import com.remote.models.History;
import com.remote.targetDetection.services.HistoryService;
import com.remote.targetDetection.services.UserService;
import com.remote.tools.utils.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipFile;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/targetDetection")
@RefreshScope
@Api(value="targetDetection",tags = "targetDetection")
public class targetDetectionController {
    //文件名列表
    public List<String>ls=new ArrayList<>();


    @Autowired
    HistoryService historyService;

    @Autowired
    UserService userService;


    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public Result<byte[]> test(){
        String path="micro-services/target_detection/src/main/resources";
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
    public Result<byte[]> pic(@RequestParam(value = "picture") MultipartFile picture) {
        //获取随机不重复的文件名
        String a= Radom.getRandomNumber(6,ls);
        String r= Radom.getRandomNumber(6,ls);

        //保存至本地
        String path="micro-services/target_detection/src/main/resources";
        String absolute=new File(path).getAbsolutePath();
        File dest1 = new File(absolute+ "/input/" + a +".jpg");//保存为jpg格式

        try {
            picture.transferTo(dest1); // 保存文件
        } catch (Exception e) {
            e.printStackTrace();
            MyFile.DeleteFolder(absolute+ "/input/" + a +".jpg");
            ls.remove(a);
            return Result.wrapErrorResult("保存图片失败");
        }

        String url="http://127.0.0.1:8300/TargetDetector";
        Http http=new Http();
        try {
            //传入文件名的参数
            Map<String, Object> map=new HashMap<>();
            map.put("a",a);
            map.put("r",r);
            //执行请求
            String res=http.doGet(url,map);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
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

    @RequestMapping(value = "/batch_work",method = RequestMethod.POST)
    public Result batchWork(String fileName, String userId, String title) throws FileNotFoundException {
        History history = new History();
        history.setId(historyService.generateID());
        history.setCreateTime(Instant.now());
        history.setOriginName1(fileName);
        history.setOriginName2("");
        history.setTitle(title);
        history.setResultName("");
        history.setSize("");
        history.setIsRemove(false);
        history.setStatus("created");
        history.setUser(userService.getById(userId));
        history.setType("target-detection");
        historyService.createOrUpdate(history);


        Thread thread = new Thread(()->{
            history.setStatus("running");
            historyService.createOrUpdate(history);
            String path="micro-services/target_detection/src/main/resources";
            String absolute=new File(path).getAbsolutePath();
            OSSConnection oss = new OSSConnection();
            oss.downLoadMatipart("target-detection", fileName);

            ZipUtil zipUtil = new ZipUtil();
            File file = new File(absolute+"/inputData.zip");
            zipUtil.unPackZip(file,absolute+"/input/");
            File inputFolder = new File(absolute + "/input/");
            File[] images = inputFolder.listFiles();
            String resultFolderDir = Radom.getRandomNumber(6,ls);
            File resultFolder = new File(absolute+ resultFolderDir);
            resultFolder.mkdir();
            for(File image:images){
                String url="http://127.0.0.1:8300/TargetDetector";
                Http http=new Http();
                try {
                    //传入文件名的参数
                    Map<String, Object> map=new HashMap<>();
                    map.put("a",image.getName());
                    map.put("r",image.getName());
                    map.put("dir",absolute+resultFolderDir);
                    //执行请求
                    String res=http.doGet(url,map);
                } catch (Exception e) {
                    e.printStackTrace();
                    MyFile.DeleteFolder(absolute+ "/input/");
                    MyFile.DeleteFolder(absolute+ "/result/");
                    File temp = new File(absolute+ "/input/");
                    temp.mkdir();
                    temp = new File(absolute+ "/result/");
                    temp.mkdir();
                    //修改状态
                    history.setStatus("fault");
                    historyService.createOrUpdate(history);
                }
            }
            FileOutputStream fos= null;
            try {
                fos = new FileOutputStream(new File(absolute+"/result.zip"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            zipUtil.toZip(absolute+ resultFolderDir,fos,true);
            oss.uplopadMatipart(fileName+"_result",absolute+"/result.zip");
            history.setStatus("success");
            history.setResultName(fileName+"_result");
            historyService.createOrUpdate(history);

            MyFile.DeleteFolder(absolute+ "/input/");
            MyFile.DeleteFolder(absolute+ "/inputData.zip");
            File temp = new File(absolute+ "/input/");
            temp.mkdir();
            MyFile.DeleteFolder(absolute+ "/result/");
            MyFile.DeleteFolder(absolute+ "/result.zip");
        });

        thread.start();
        return Result.wrapSuccessfulResult("已开始运算，在历史记录中查看运算状态，下载运算结果。");
    }
}
