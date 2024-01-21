package com.qkwl.web.front.controller;

import com.qkwl.common.dto.Enum.SystemCoinTypeEnum;
import com.qkwl.common.dto.capital.FUserBankinfoDTO;
import com.qkwl.common.dto.capital.FUserPushDTO;
import com.qkwl.common.dto.capital.FUserVirtualAddressWithdrawDTO;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemCoinTypeVO;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.finances.FUserFinancesDTO;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.user.FUserDTO;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.rpc.capital.IUserCapitalAccountService;
import com.qkwl.common.rpc.capital.IUserFinancesService;
import com.qkwl.common.rpc.capital.IUserPushService;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.web.front.controller.base.WebBaseController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FrontFinancialController extends WebBaseController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserPushService userPushService;
    @Autowired
    private IUserWalletService userWalletService;
    @Autowired
    private IUserCapitalAccountService userCapitalAccountService;
    @Autowired
    private IUserFinancesService userFinancesService;
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 个人资产
     */
    @RequestMapping("/financial/index")
    public ModelAndView index() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        FUser fuser = getCurrentUserInfoByToken();
        List<UserCoinWallet> userWalletList = userWalletService.listUserCoinWallet(fuser.getFid());
        // 数据
        Iterator iterator = userWalletList.iterator();
        while (iterator.hasNext()) {
            UserCoinWallet wallet = (UserCoinWallet) iterator.next();
            if (!redisHelper.hasCoinId(wallet.getCoinId())) {
                iterator.remove();
            }
        }
        modelAndView.addObject("userWalletList", userWalletList);
        modelAndView.setViewName("front/financial/index");
        return modelAndView;
    }

    /**
     * 资金帐号-虚拟币地址管理
     */
    @RequestMapping("/financial/accountcoin")
    public ModelAndView accountcoin(
            @RequestParam(required = false, defaultValue = "0") int symbol
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        FUser fuser = getCurrentUserInfoByToken();
        fuser = userService.selectUserById(fuser.getFid());
        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || !coinType.getType().equals(SystemCoinTypeEnum.COIN.getCode())) {
            coinType = redisHelper.getCoinTypeIsWithdrawFirst(SystemCoinTypeEnum.COIN.getCode());
        }
        if (coinType != null) {
            List<FUserVirtualAddressWithdrawDTO> withdraws = userCapitalAccountService.listCoinAddressWithdraw(fuser.getFid(), coinType.getId());
            modelAndView.addObject("fuser", ModelMapperUtils.mapper(fuser, FUserDTO.class));
            modelAndView.addObject("coinType", ModelMapperUtils.mapper(coinType, SystemCoinTypeVO.class));
            modelAndView.addObject("fvirtualaddressWithdraws", withdraws);
        }
        modelAndView.addObject("coinTypeList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class));
        modelAndView.setViewName("front/financial/account_assets_coin");
        return modelAndView;
    }

    /**
     * 存币理财
     */
    @RequestMapping("/financial/finances")
    public ModelAndView finances(
            @RequestParam(required = false, defaultValue = "1") Integer currentPage
    ) {
        ModelAndView modelAndView = new ModelAndView();

        // 币种
        List<SystemCoinType> coinTypes = redisHelper.getCoinTypeFinancesList();
        if (coinTypes == null || coinTypes.size() == 0) {
            modelAndView.setViewName("redirect:/");
            return modelAndView;
        }
        // 默认第一条
        SystemCoinType coinType = coinTypes.get(0);
        // 记录
        FUser fuser = getCurrentUserInfoByToken();
        Pagination<FUserFinancesDTO> page = new Pagination<>(currentPage, Constant.webPageSize,
                "/financial/finances.html?");
        FUserFinancesDTO userFinances = new FUserFinancesDTO();
        userFinances.setFuid(fuser.getFid());
        page = userFinancesService.ListUserFinances(page, userFinances);
        // 钱包
        UserCoinWallet userWallet = userWalletService.getUserCoinWallet(fuser.getFid(), coinType.getId());
        Map<Integer, String> financesCoinMap = new LinkedHashMap<>();
        for (SystemCoinType systemCoinType : coinTypes) {
            financesCoinMap.put(systemCoinType.getId(), systemCoinType.getName());
        }
        modelAndView.addObject("userWallet", userWallet);
        modelAndView.addObject("page", page);
        modelAndView.addObject("financesCoinMap", financesCoinMap);
        modelAndView.addObject("typeList", redisHelper.getVirtualFinancesList(coinType.getId()));
        modelAndView.setViewName("front/financial/finances");
        return modelAndView;
    }
}
