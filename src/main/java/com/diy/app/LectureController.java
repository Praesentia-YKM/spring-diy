package com.diy.app;

import com.diy.framework.web.mvc.annotation.Controller;
import com.diy.framework.web.mvc.annotation.RequestMapping;
import com.diy.framework.web.mvc.annotation.RequestMethod;
import com.diy.framework.web.mvc.view.ModelAndView;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(final LectureService lectureService) {
        this.lectureService = lectureService;
        System.out.println("lectureController::lectureService = " + lectureService);
    }

    @RequestMapping(methods = RequestMethod.GET)
    public ModelAndView list(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final Map<String, Object> model = new HashMap<>();
        model.put("lectures", lectureService.getLectures());
        return new ModelAndView("lecture-list", model);
    }

    @RequestMapping(methods = RequestMethod.POST)
    public ModelAndView create(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final byte[] bodyBytes = req.getInputStream().readAllBytes();
        final String body = new String(bodyBytes, StandardCharsets.UTF_8);
        final Lecture lecture = new ObjectMapper().readValue(body, Lecture.class);
        lectureService.registerLecture(lecture);

        return new ModelAndView("redirect:/lectures");
    }
}
