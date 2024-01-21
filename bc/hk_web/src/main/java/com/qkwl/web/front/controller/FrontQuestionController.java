package com.qkwl.web.front.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qkwl.common.util.Constant;
import com.qkwl.common.dto.Enum.QuestionTypeEnum;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.user.FQuestion;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.rpc.user.IQuestionService;
import com.qkwl.web.front.controller.base.WebBaseController;

@Controller
public class FrontQuestionController extends WebBaseController {

    @Autowired
    private IQuestionService questionService;

    @RequestMapping("/online_help/index")
    public ModelAndView question(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();

        Map<Integer, Object> fquestiontypes = new LinkedHashMap<Integer, Object>();
        for (QuestionTypeEnum questionTypeEnum : QuestionTypeEnum.values()) {
            fquestiontypes.put(questionTypeEnum.getCode(), GetR18nMsg("question.type.enum" + questionTypeEnum.getCode()));
        }

        modelAndView.addObject("fquestiontypes", fquestiontypes);
        modelAndView.setViewName("front/question/question");
        return modelAndView;
    }

    @RequestMapping("/online_help/help_list")
    public ModelAndView questionColumn(
            @RequestParam(required = false, defaultValue = "1") Integer currentPage
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        FUser fuser = getCurrentUserInfoByToken();
        if (fuser != null) {
            Pagination<FQuestion> fquestion = new Pagination<FQuestion>(currentPage, Constant.QuestionRecordPerPage, "/online_help/help_list.html?");
            FQuestion operation = new FQuestion();
            operation.setFuid(fuser.getFid());
            fquestion = questionService.selectPageQuestionList(fquestion, operation);

            modelAndView.addObject("currentPage", currentPage);
            modelAndView.addObject("list", fquestion);
        }
        modelAndView.setViewName("front/question/questionlist");
        return modelAndView;
    }

}
