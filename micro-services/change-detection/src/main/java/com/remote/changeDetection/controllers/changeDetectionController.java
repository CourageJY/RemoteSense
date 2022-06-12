package com.remote.changeDetection.controllers;

import com.remote.tools.utils.Result;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/changeDetection")
@RefreshScope
@Api(value = "changeDetection", tags = "changeDetection")
public class changeDetectionController {

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public Result<String> test(@RequestParam("file") List<MultipartFile> file) {
        System.out.println(file.size());
        return Result.wrapSuccessfulResult("It's successful!");
    }
}
