package com.qkwl.web.front.controller;

import java.util.*;

import com.qkwl.common.Enum.validate.LocaleEnum;
import com.qkwl.common.dto.Enum.*;
import com.qkwl.common.dto.common.PageHelper;
import com.qkwl.common.dto.user.CommissionRecord;
import com.qkwl.common.i18n.LuangeHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.util.StringUtils;
import com.qkwl.common.model.KeyValues;
import com.qkwl.common.result.Result;
import com.qkwl.common.rpc.capital.IUserCapitalAccountService;
import com.qkwl.common.rpc.capital.IUserCapitalService;
import com.qkwl.common.rpc.capital.IUserWalletService;
import com.qkwl.common.util.ArgsConstant;
import com.qkwl.common.util.Constant;
import com.qkwl.common.util.ModelMapperUtils;
import com.qkwl.common.util.Utils;
import com.qkwl.common.dto.capital.FVirtualCapitalOperationDTO;
import com.qkwl.common.dto.capital.FWalletCapitalOperationDTO;
import com.qkwl.common.dto.coin.SystemCoinSetting;
import com.qkwl.common.dto.coin.SystemCoinType;
import com.qkwl.common.dto.coin.SystemCoinTypeVO;
import com.qkwl.common.dto.common.Pagination;
import com.qkwl.common.dto.system.FSystemBankinfoRecharge;
import com.qkwl.common.dto.system.FSystemBankinfoWithdraw;
import com.qkwl.common.dto.user.FUser;
import com.qkwl.common.dto.capital.FUserBankinfoDTO;
import com.qkwl.common.dto.user.FUserDTO;
import com.qkwl.common.dto.capital.FUserVirtualAddressWithdrawDTO;
import com.qkwl.common.dto.wallet.UserCoinWallet;
import com.qkwl.common.rpc.user.IUserService;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.web.front.controller.base.WebBaseController;
import com.qkwl.web.utils.WebConstant;


@Controller
public class FrontAccountController extends WebBaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(FrontAccountController.class);

    @Autowired
    private IUserCapitalService userCapitalService;
    @Autowired
    private IUserCapitalAccountService userCapitalAccountService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private IUserWalletService userWalletService;

    /**
     * RMB充值
     */
    @RequestMapping("/deposit/cny_deposit")
    public ModelAndView cnyDeposit(
            @RequestParam(required = false, defaultValue = "0") Integer symbol,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage
    ) {
        ModelAndView modelAndView = new ModelAndView();
        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || !coinType.getIsRecharge()
                || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())
                || !coinType.getType().equals(SystemCoinTypeEnum.CNY.getCode())) {
            coinType = redisHelper.getCoinTypeIsRechargeFirst(SystemCoinTypeEnum.CNY.getCode());
            if (coinType == null) {
                modelAndView.setViewName("redirect:/deposit/coin_deposit.html");
                return modelAndView;
            }
        }
        // 系统银行账号
        Integer rechargeType = 0;
        List<FSystemBankinfoRecharge> systemBankinfoRecharges = redisHelper.getRechargeBank(rechargeType);
        if (systemBankinfoRecharges == null || systemBankinfoRecharges.size() == 0) {
            modelAndView.setViewName("redirect:/deposit/coin_deposit.html");
            return modelAndView;
        }
        // 用户转账的银行类型
        List<FSystemBankinfoWithdraw> systemBankinfoWithdraws = redisHelper.getWithdrawBankList();
        // 用户
        FUser fuser = getCurrentUserInfoByToken();
        // 用户银行卡
        List<FUserBankinfoDTO> userBankinfo = userCapitalAccountService.listBankInfo(fuser.getFid(), coinType.getId(), rechargeType);
        // 随机数小数
        int randomDecimal = (new Random().nextInt(80) + 11);
        // 充值记录
        Pagination<FWalletCapitalOperationDTO> page = new Pagination<>(currentPage,
                Constant.CapitalRecordPerPage, "/deposit/cny_deposit.html?symbol=" + symbol + "&");
        FWalletCapitalOperationDTO operation = new FWalletCapitalOperationDTO();
        operation.setFuid(fuser.getFid());
        operation.setFinouttype(CapitalOperationInOutTypeEnum.IN.getCode());
        operation.setFcoinid(coinType.getId());
        page = userCapitalService.listWalletCapitalOperation(page, operation);

        modelAndView.addObject("minRecharge", WebConstant.MINRECHARGECNY);
        modelAndView.addObject("maxRecharge", WebConstant.MAXRECHARGECNY);
        modelAndView.addObject("randomDecimal", randomDecimal);
        modelAndView.addObject("rechargeType", rechargeType);
        modelAndView.addObject("page", page);
        modelAndView.addObject("telephone", fuser.getFtelephone());
        modelAndView.addObject("realname", fuser.getFrealname());
        modelAndView.addObject("isTelephone", fuser.getFistelephonebind());
        modelAndView.addObject("isGoogle", fuser.getFgooglebind());
        modelAndView.addObject("systemBankinfoWithdraws", systemBankinfoWithdraws);
        modelAndView.addObject("systemBankinfoRecharges", systemBankinfoRecharges);
        modelAndView.addObject("userBankinfo", userBankinfo);
        modelAndView.addObject("coinType", ModelMapperUtils.mapper(coinType, SystemCoinTypeVO.class));
        modelAndView.addObject("cnyList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsRechargeList(SystemCoinTypeEnum.CNY.getCode()), SystemCoinTypeVO.class));
        modelAndView.addObject("coinList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsRechargeList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class));
        modelAndView.setViewName("front/account/account-agency-deposit");
        return modelAndView;
    }

    /**
     * 人民币提现
     */
    @RequestMapping("/withdraw/cny_withdraw")
    public ModelAndView withdrawCny(
            @RequestParam(required = false, defaultValue = "0") Integer symbol,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        // 币种
        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || !coinType.getIsWithdraw()
                || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())
                || !coinType.getType().equals(SystemCoinTypeEnum.CNY.getCode())) {
            coinType = redisHelper.getCoinTypeIsWithdrawFirst(SystemCoinTypeEnum.CNY.getCode());
            if (coinType == null) {
                modelAndView.setViewName("redirect:/withdraw/coin_withdraw.html");
                return modelAndView;
            }
        }
        // 用户
        FUser fuser = getCurrentUserInfoByToken();
        fuser = userService.selectUserById(fuser.getFid());
        // 体现银行
        List<FUserBankinfoDTO> fbankinfoWithdraw = userCapitalAccountService.listBankInfo(fuser.getFid(), coinType.getId(), 0);
        String currentLocal = LuangeHelper.getLan(sessionContextUtils.getContextRequest());
        for (FUserBankinfoDTO bankinfo : fbankinfoWithdraw) {
            FSystemBankinfoWithdraw item = redisHelper.getWithdrawBankById(bankinfo.getFbanktype());
            if (currentLocal.equals(LocaleEnum.ZH_CN.getName())) {
                bankinfo.setFbanktype_s(item.getFcnname());
            } else if (currentLocal.equals(LocaleEnum.ZH_TW.getName())) {
                bankinfo.setFbanktype_s(item.getFtwname());
            } else if (currentLocal.equals(LocaleEnum.EN_US.getName())) {
                bankinfo.setFbanktype_s(item.getFenname());
            }
        }
        //用户转账的银行类型
        List<FSystemBankinfoWithdraw> bankTypes = redisHelper.getWithdrawBankList();
        // 提现记录
        Pagination<FWalletCapitalOperationDTO> page = new Pagination<FWalletCapitalOperationDTO>(currentPage,
                Constant.CapitalWithdrawPerPage, "/withdraw/cny_withdraw.html?symbol=" + symbol + "&");
        FWalletCapitalOperationDTO operation = new FWalletCapitalOperationDTO();
        operation.setFuid(fuser.getFid());
        operation.setFinouttype(CapitalOperationInOutTypeEnum.OUT.getCode());
        page = userCapitalService.listWalletCapitalOperation(page, operation);
        // 钱包
        UserCoinWallet userWallet = userWalletService.getUserCoinWallet(fuser.getFid(), coinType.getId());
        // VIP设置
        SystemCoinSetting withdrawSetting = redisHelper.getCoinSetting(coinType.getId(), fuser.getLevel());
        // 界面数据
        modelAndView.addObject("withdrawSetting", withdrawSetting);
        modelAndView.addObject("wallet", userWallet);
        modelAndView.addObject("page", page);
        modelAndView.addObject("bankTypes", bankTypes);
        modelAndView.addObject("fbankinfoWithdraw", fbankinfoWithdraw);
        modelAndView.addObject("fuser", ModelMapperUtils.mapper(fuser, FUserDTO.class));
        modelAndView.addObject("coinType", ModelMapperUtils.mapper(coinType, SystemCoinTypeVO.class));
        modelAndView.addObject("cnyList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.CNY.getCode()), SystemCoinTypeVO.class));
        modelAndView.addObject("coinList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class));
        modelAndView.setViewName("front/account/account_withdrawcny");
        return modelAndView;
    }

    /**
     * 虚拟币充值
     *
     * @param symbol
     * @return
     * @throws Exception
     */
    @RequestMapping("/deposit/coin_deposit")
    public ModelAndView rechargeBtc(@RequestParam(required = false, defaultValue = "0") Integer symbol) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || !coinType.getIsRecharge()
                || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())
                || !coinType.getType().equals(SystemCoinTypeEnum.COIN.getCode())) {
            coinType = redisHelper.getCoinTypeIsRechargeFirst(SystemCoinTypeEnum.COIN.getCode());
            if (coinType == null) {
                modelAndView.setViewName("redirect:/");
                return modelAndView;
            }
        }
        FUser user = getCurrentUserInfoByToken();
        // 充值地址
        String ip = Utils.getIpAddr(sessionContextUtils.getContextRequest());
        Result result = userCapitalAccountService.createCoinAddressRecharge(user.getFid(), coinType.getId(), ip);
        if (result.getSuccess()) {
            modelAndView.addObject("rechargeAddress", result.getData());
        }
        // 最近十次充值记录
        Pagination<FVirtualCapitalOperationDTO> page = new Pagination<>(1, Constant.CapitalRecordPerPage);
        FVirtualCapitalOperationDTO operation = new FVirtualCapitalOperationDTO();
        operation.setFuid(user.getFid());
        operation.setFtype(VirtualCapitalOperationTypeEnum.COIN_IN.getCode());
        operation.setFcoinid(coinType.getId());
        page = userCapitalService.listVirtualCapitalOperation(page, operation);

        modelAndView.addObject("page", page);
        modelAndView.addObject("coinType", ModelMapperUtils.mapper(coinType, SystemCoinTypeVO.class));
        modelAndView.addObject("cnyList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsRechargeList(SystemCoinTypeEnum.CNY.getCode()), SystemCoinTypeVO.class));
        modelAndView.addObject("coinList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsRechargeList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class));
        modelAndView.setViewName("front/account/account_rechargebtc");
        return modelAndView;
    }

    /**
     * 虚拟币提现
     */
    @RequestMapping("/withdraw/coin_withdraw")
    public ModelAndView withdrawBtc(
            @RequestParam(required = false, defaultValue = "0") Integer symbol) {
        ModelAndView modelAndView = new ModelAndView();
        // 币种查找
        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || !coinType.getIsWithdraw()
                || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())
                || !coinType.getType().equals(SystemCoinTypeEnum.COIN.getCode())) {
            coinType = redisHelper.getCoinTypeIsWithdrawFirst(SystemCoinTypeEnum.COIN.getCode());
            if (coinType == null) {
                modelAndView.setViewName("redirect:/");
                return modelAndView;
            }
        }
        // 用户
        FUser user = getCurrentUserInfoByToken();
        // 钱包
        UserCoinWallet userWallet = userWalletService.getUserCoinWallet(user.getFid(), coinType.getId());
        // 地址
        List<FUserVirtualAddressWithdrawDTO> withdrawAddress = userCapitalAccountService.listCoinAddressWithdraw(user.getFid(), coinType.getId());
        // VIP设置
        SystemCoinSetting withdrawSetting = redisHelper.getCoinSetting(coinType.getId(), user.getLevel());
        // 近10条提现记录
        Pagination<FVirtualCapitalOperationDTO> page = new Pagination<FVirtualCapitalOperationDTO>(1, Constant.webPageSize,
                "/withdraw/coin_withdraw.html?symbol" + symbol + "&");
        FVirtualCapitalOperationDTO operation = new FVirtualCapitalOperationDTO();
        operation.setFuid(user.getFid());
        operation.setFtype(VirtualCapitalOperationTypeEnum.COIN_OUT.getCode());
        operation.setFcoinid(coinType.getId());
        page = userCapitalService.listVirtualCapitalOperation(page, operation);
        // 页面数据
        modelAndView.addObject("page", page);
        modelAndView.addObject("fuser", ModelMapperUtils.mapper(user, FUserDTO.class));
        modelAndView.addObject("userWallet", userWallet);
        modelAndView.addObject("withdrawAddress", withdrawAddress);
        modelAndView.addObject("withdrawSetting", withdrawSetting);
        modelAndView.addObject("coinType", ModelMapperUtils.mapper(coinType, SystemCoinTypeVO.class));
        modelAndView.addObject("cnyList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.CNY.getCode()), SystemCoinTypeVO.class));
        modelAndView.addObject("coinList", ModelMapperUtils.mapper(
                redisHelper.getCoinTypeIsWithdrawList(SystemCoinTypeEnum.COIN.getCode()), SystemCoinTypeVO.class));
        modelAndView.setViewName("front/account/account_withdrawbtc");
        return modelAndView;
    }

    @RequestMapping("/financial/record")
    public ModelAndView record(
            @RequestParam(required = false, defaultValue = "0") Integer symbol,
            @RequestParam(required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(required = false, defaultValue = "1") Integer type,
            @RequestParam(required = false, defaultValue = "") String begindate,
            @RequestParam(required = false, defaultValue = "") String enddate,
            @RequestParam(required = false, defaultValue = "2") Integer datetype) throws Exception {
        ModelAndView modelAndView = new ModelAndView();

        SystemCoinType coinType = redisHelper.getCoinType(symbol);
        if (coinType == null || coinType.getStatus().equals(SystemCoinStatusEnum.ABNORMAL.getCode())) {
            coinType = redisHelper.getCoinTypeFirst();
            if (coinType == null) {
                modelAndView.setViewName("redirect:/");
                return modelAndView;
            }
            symbol = coinType.getId();
        }
        if (datetype > 0) {
            if (datetype == 1) {
                begindate = Utils.getCurTimeString(0);
                enddate = Utils.getCurTimeString(0);
            }
            if (datetype == 2) {
                begindate = Utils.getCurTimeString(-7);
                enddate = Utils.getCurTimeString(0);
            }
            if (datetype == 3) {
                begindate = Utils.getCurTimeString(-15);
                enddate = Utils.getCurTimeString(0);
            }
            if (datetype == 4) {
                begindate = Utils.getCurTimeString(-30);
                enddate = Utils.getCurTimeString(0);
            }
        }
        List<SystemCoinType> coinTypes = redisHelper.getCoinTypeList();
        // 过滤器
        List<KeyValues> filters = new ArrayList<KeyValues>();

        String keyFormat = "/financial/record.html";
        for (SystemCoinType systemCoinType : coinTypes) {
            String valueR = GetR18nMsg("financial.account.recordselectr");
            if (!StringUtils.isEmpty(valueR)) {
                KeyValues keyValues = new KeyValues();
                keyValues.setKey(keyFormat + "?symbol=" + systemCoinType.getId() + "&type=1");
                keyValues.setValue(String.format(valueR, systemCoinType.getShortName()));
                filters.add(keyValues);
            }
            String valueW = GetR18nMsg("financial.account.recordselectw");
            if (!StringUtils.isEmpty(valueR)) {
                KeyValues keyValues = new KeyValues();
                keyValues.setKey(keyFormat + "?symbol=" + systemCoinType.getId() + "&type=2");
                keyValues.setValue(String.format(valueW, systemCoinType.getShortName()));
                filters.add(keyValues);
            }

        }
        FUser fuser = getCurrentUserInfoByToken();
        if (coinType.getType().equals(SystemCoinTypeEnum.CNY.getCode())) {
            FWalletCapitalOperationDTO operation = new FWalletCapitalOperationDTO();
            operation.setFuid(fuser.getFid());
            operation.setFinouttype(type);
            operation.setFcoinid(symbol);
            Pagination<FWalletCapitalOperationDTO> page = new Pagination<>(currentPage,
                    Constant.CapitalRecordPerPage, begindate, enddate + " 23:59:59",
                    "/financial/record.html?symbol=" + symbol + "&type=" + type + "&datetype=" + datetype + "&begindate="
                            + begindate + "&enddate=" + enddate + "&");
            page = userCapitalService.listWalletCapitalOperation(page, operation);
            modelAndView.addObject("list", page);
            modelAndView.addObject("recordType", SystemCoinTypeEnum.CNY.getCode());
        } else if (coinType.getType().equals(SystemCoinTypeEnum.COIN.getCode())) {
            FVirtualCapitalOperationDTO operation = new FVirtualCapitalOperationDTO();
            operation.setFuid(fuser.getFid());
            operation.setFtype(type);
            operation.setFcoinid(coinType.getId());
            operation.setFcoinid(symbol);
            Pagination<FVirtualCapitalOperationDTO> page = new Pagination<FVirtualCapitalOperationDTO>(currentPage,
                    Constant.CapitalRecordPerPage, begindate, enddate + " 23:59:59",
                    "/financial/record.html?type=" + type + "&symbol=" + symbol + "&datetype=" + datetype + "&begindate="
                            + begindate + "&enddate=" + enddate + "&");
            page = userCapitalService.listVirtualCapitalOperation(page, operation);
            modelAndView.addObject("list", page);
            modelAndView.addObject("recordType", SystemCoinTypeEnum.COIN.getCode());
        }
        String select = "";
        if (type == 1) {
            select = GetR18nMsg("financial.account.recordselectr");
        }
        if (type == 2) {
            select = GetR18nMsg("financial.account.recordselectw");
        }
        modelAndView.addObject("select", String.format(select, coinType.getShortName()));
        modelAndView.addObject("type", type);
        modelAndView.addObject("symbol", symbol);
        modelAndView.addObject("filters", filters);
        modelAndView.addObject("fvirtualcointype", ModelMapperUtils.mapper(coinTypes, SystemCoinTypeVO.class));
        modelAndView.addObject("begindate", begindate);// 时间
        modelAndView.addObject("enddate", enddate);
        modelAndView.addObject("datetype", datetype);
        modelAndView.setViewName("front/account/account_record");
        return modelAndView;
    }

    @RequestMapping("/financial/commission")
    public ModelAndView commission(
            @RequestParam(required = false, defaultValue = "1") Integer currentPage) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        List<SystemCoinType> coinTypes = redisHelper.getCoinTypeList();

        FUser fuser = getCurrentUserInfoByToken();

        Pagination<CommissionRecord> commissionRecordPagination = userCapitalService.listCommissionRecord(new Pagination<CommissionRecord>(currentPage, Constant.CapitalRecordPerPage)
                , fuser.getFid(), null);

        Collection<CommissionRecord> data = commissionRecordPagination.getData();
        Iterator<CommissionRecord> iterator = data.iterator();

        while (iterator.hasNext()) {
            CommissionRecord next = iterator.next();
            for (SystemCoinType coinType : coinTypes) {
                if (coinType.getId() == next.getCoinid()) {
                    next.setCoinname(coinType.getShortName().toUpperCase());
                    break;
                }
            }
        }

        modelAndView.addObject("list", commissionRecordPagination);
        modelAndView.addObject("introurl", "https://hotcoin.top/user/intro.html?intro=" + fuser.getFid());
        modelAndView.setViewName("front/account/account_commission");
        return modelAndView;
    }

}
