package com.qkwl.web.front.controller;

import com.qkwl.common.dto.capital.FRewardCodeDTO;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.rpc.capital.IUserRewardCodeService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.web.front.controller.base.WebBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

@Controller
public class FrontActivityController extends WebBaseController {
    @Autowired
    private IUserRewardCodeService rewardCodeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 充值码充值
     */
    @RequestMapping(value = "/activity/index")
    public ModelAndView activityGo(@RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        int pagesize = 5;
        ModelAndView modelAndView = new ModelAndView();
        FUser fuser = super.getCurrentUserInfoByToken();
        fuser = userService.selectUserById(fuser.getFid());

        Pagination<FRewardCodeDTO> page = new Pagination<FRewardCodeDTO>(currentPage, pagesize, "/activity/index.html?");
        FRewardCodeDTO code = new FRewardCodeDTO();
        code.setFuid(fuser.getFid());
        code.setFstate(true);
        page = rewardCodeService.listRewardeCode(page, code);
        Collection<FRewardCodeDTO> list = page.getData();
        if (list != null) {
            for (FRewardCodeDTO fRewardCode : list) {
                SystemCoinType coinType = redisHelper.getCoinType(fRewardCode.getFtype());
                fRewardCode.setFtype_s(coinType.getShortName());
            }
        }
        modelAndView.addObject("pagin", page.getPagin());
        modelAndView.addObject("frewardcodes", list);
        modelAndView.setViewName("/front/activity/index");
        return modelAndView;
    }
}
