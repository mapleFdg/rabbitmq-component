package com.imooc.controller.center;
/**
 * @author hzc
 * @date 2020-07-06 23:45
 */

import com.imooc.bo.center.CenterUserBo;
import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hzc
 * @date 2020-07-06 23:45
 */
@Api(value = "用户中心用户API", tags = {"用户中心用户API"})
@RestController
@RequestMapping("userInfo")
@Slf4j
public class CenterUserContorller extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "用户头像上传", notes = "用户头像上传")
    @PostMapping("uploadFace")
    public JSONResult uploadFace(
            @ApiParam(name = "userId", value = "用户ID", required = true)
            @RequestParam String userId,
            @ApiParam(name = "file", value = "用户头像", required = true)
                    MultipartFile file,
            HttpServletRequest request, HttpServletResponse response) {

        String fileSpace = fileUpload.getImageUserFaceLocation();
//        String fileSpace = IMAGE_USER_FACE_LOCATION;
        String uploadPathPrefix = userId;
        String faceUrl;

        if (file == null) {
            return JSONResult.errorMsg("文件为空");
        }

        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            return JSONResult.errorMsg("文件名为空");
        }
        String[] fileNameArr = fileName.split("\\.");
        String suffix = fileNameArr[fileNameArr.length - 1];

        if(!"jpg".equalsIgnoreCase(suffix) && !"png".equalsIgnoreCase(suffix) && !"jpge".equalsIgnoreCase(suffix)){
            return JSONResult.errorMsg("图片格式不支持");
        }

        String newFileName = "face-" + System.currentTimeMillis() + "-" + userId + "." + suffix;
        String fullFileName = fileSpace + userId + File.separator + newFileName;

        File outFile = new File(fullFileName);
        // 判断上级文件夹是否存在，不存在创建
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(outFile);
             InputStream inputStream = file.getInputStream();) {
            IOUtils.copy(inputStream, out);
            faceUrl = fileUpload.getImageServerUrl() +  uploadPathPrefix + "/" + newFileName;
            Users users = centerUserService.updateUserFace(userId, faceUrl);

            UserVO userVO = conventUserVO(users);

            //设置cookies
            CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(userVO),true);


            return JSONResult.ok();
        } catch (IOException e) {
            log.error("上传头像失败，{}", e.getMessage());
            e.printStackTrace();
            return JSONResult.errorMsg("上传文件失败，请稍后重试");
        }

    }


    @ApiOperation(value = "用户信息更新", notes = "用户信息更新")
    @PostMapping("update")
    public JSONResult update(
            @ApiParam(name = "userId", value = "用户ID")
            @RequestParam String userId,
            @RequestBody @Valid CenterUserBo centerUserBo,
            BindingResult bindingResult,
            HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = getErrors(bindingResult);
            log.error("update User error : {}", errorMap);
            return JSONResult.errorMap(errorMap);
        }

        Users users = centerUserService.updateUserInfo(userId, centerUserBo);

        UserVO userVO = conventUserVO(users);

        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(userVO), true);

        return JSONResult.ok(users);
    }

}
