package com.ewallet.WalletService.Repository;

import com.ewallet.WalletService.Model.Wallet;
import com.ewallet.WalletService.Model.WalletInRedis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;


@Repository
public class WalletDaoRedis {

    private static final String KEY = "wallet";
    private static final Logger logger = LoggerFactory.getLogger(WalletDaoRedis.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    WalletRepository walletRepository;

    public WalletInRedis getWallet(Integer uid) {
        String id = Integer.toString(uid);
        Map walletMap = (Map) redisTemplate.opsForHash().get(KEY,id);
        WalletInRedis walletInRedis;

        if(walletMap==null || walletMap.equals(null) || walletMap.isEmpty()) {
            logger.info("Fetching walletMap from DB");
            walletInRedis = new WalletInRedis();
            Wallet wallet = walletRepository.findByUserId(uid);

            walletInRedis.setAmount(wallet.getBalance());
            walletInRedis.setUid(wallet.getUser_id());
            Map walletHash = new ObjectMapper().convertValue(walletInRedis, Map.class);
            redisTemplate.opsForHash().put(KEY,Integer.toString(walletInRedis.getUid()),walletHash);
        }
        else  {
            logger.info("Fetching from Cache");
            walletInRedis = new ObjectMapper().convertValue(walletMap,WalletInRedis.class);
        }
        return walletInRedis;
    }

    public Boolean updateWallet(WalletInRedis wallet) {
        try {
            Map walletHash = new ObjectMapper().convertValue(wallet, Map.class);
            redisTemplate.opsForHash().put(KEY, Integer.toString(wallet.getUid()), walletHash);
            logger.info("Wallet updated in Redis");
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
