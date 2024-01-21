package com.qkwl.service.score.mapper;

import com.qkwl.common.dto.wallet.UserCoinWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 用户钱包数据操作接口
 * @author ZKF
 */
@Mapper
public interface UserCoinWalletMapper {

    /**
     * 根据用户ID查询所有钱包
     * @param uid
     * @return
     */
    List<UserCoinWallet> selectUid(@Param("uid") Integer uid);

}
