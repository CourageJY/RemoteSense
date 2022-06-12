package com.remote.changeDetection.controllers;

import com.remote.tools.utils.Result;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class changeDetectionControllerTest {

    @Test
    void pic() {
        String path="E:\\Programs\\RemoteSensing\\RemoteSensing-backend\\micro-services\\change-detection\\src\\main\\resources\\";

        //调用并运行python文件
        try {
            String[] arg = new String[] { "python", path+"ChangeDetector.py"};
            Process proc = Runtime.getRuntime().exec(arg);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }


        //读取python运行文件，并以字符流返还至前端
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new FileInputStream(new File(path+"example\\"+ "result.png")));
        } catch (IOException e) {
            e.printStackTrace();

        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            assert bufferedImage != null;
            ImageIO.write(Objects.requireNonNull(bufferedImage), "png", out);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}