package com.ewallet.WalletService.Controller;

import com.ewallet.WalletService.Exception.WalletBadRequest;
import com.ewallet.WalletService.Exception.WalletNotFoundException;
import com.ewallet.WalletService.Model.Wallet;
import com.ewallet.WalletService.Model.WalletInRedis;
import com.ewallet.WalletService.Repository.TransactionRepository;
import com.ewallet.WalletService.Repository.WalletDaoRedis;
import com.ewallet.WalletService.Repository.WalletRepository;
import com.ewallet.WalletService.Util.WalletValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WalletController {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private WalletDaoRedis walletDaoRedis;

    WalletValidator walletValidator = new WalletValidator();
    private  static final String TOPIC = "test";
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    RedissonClient redissonClient = Redisson.create();

    @ApiOperation(value = "Find all the wallet")
    @GetMapping("/findAllWallet")
    List<Wallet> findAllWallet() {
        return walletRepository.findAll();
    }

    //Find a given wallet
    @GetMapping("/wallet/{id}")
    @ApiOperation(value = "Find wallet by Id")
    Wallet findOneWallet(@ApiParam(value = "Store id of the point of service to deliver to/collect from", required = true)@PathVariable int id) {
        logger.info("/wallet/{id} called with id "+ id);
        // Optional<User> user = repository.findById(id);
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    // Save
    @PostMapping("/createWallet")
    //Return 201 instead of 200
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create New Wallet")
    Wallet createWallet(@RequestBody Wallet newWallet) {
        if(!walletValidator.validateWalletRequest(newWallet)) {
            logger.info("CreateNewWallet request not valid");
            throw new WalletBadRequest();
        }
        Wallet wallet = walletRepository.save(newWallet);

        // Saving balance in Redis Cache
        WalletInRedis walletInRedis = new WalletInRedis();
        walletInRedis.setAmount(newWallet.getBalance());
        walletInRedis.setUid(newWallet.getUser_id());
        walletDaoRedis.updateWallet(walletInRedis);
        return wallet;
    }

    //Save
    @PutMapping("/updateWallet")
    @ApiOperation(value = "Update Wallet")
    Wallet updateWallet(@RequestBody Wallet newWallet) {
        Wallet wallet = walletRepository.save(newWallet);

        //Saving balance in Redis Cache
        WalletInRedis walletInRedis = walletDaoRedis.getWallet(newWallet.getUser_id());
        if(walletInRedis == null) {
            walletInRedis = new WalletInRedis();
            walletInRedis.setUid(newWallet.getUser_id());
        }
        walletInRedis.setAmount(newWallet.getBalance());
        walletDaoRedis.updateWallet(walletInRedis);
        return wallet;
    }
}
