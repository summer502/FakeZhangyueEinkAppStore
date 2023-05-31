package pers.summer502.j8zyeinkappstore.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import pers.summer502.j8zyeinkappstore.model.AppInfoDTO;

public interface HomeService {

    void homeHtml(String pathName, HttpServletResponse response);

    boolean saveAppInfo(AppInfoDTO appInfoDTO, MultipartFile iconFile, MultipartFile appUrlFile);
}
