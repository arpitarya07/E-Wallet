package com.ewallet.WalletService.Controller;

import com.ewallet.WalletService.Exception.TransactionBadRequest;
import com.ewallet.WalletService.Model.*;
import com.ewallet.WalletService.Repository.TransactionRepository;
import com.ewallet.WalletService.Repository.WalletDaoRedis;
import com.ewallet.WalletService.Repository.WalletRepository;
import com.ewallet.WalletService.Service.UserService;
import com.ewallet.WalletService.Util.TransactionValidator;
import com.ewallet.WalletService.Util.WalletValidator;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;

@RestController
public class TransactionController {

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    private WalletDaoRedis walletDaoRedis;
    @Autowired
    private UserService userService;

    WalletValidator walletValidator = new WalletValidator();
    TransactionValidator transactionValidator = new TransactionValidator();
    private static final String TOPIC = "test";
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping("/sendMoney")
    @ApiOperation(value = "To send money from one user to another")
    //return 201 instead of 200
    @ResponseStatus(HttpStatus.CREATED)
    Transaction addBal(@RequestBody Transaction transaction) throws Exception {
        if(!transactionValidator.validateRequest(transaction)) {
            throw new TransactionBadRequest();
        }
        transaction.setDate(new Date(Calendar.getInstance().getTime().getTime()));
        User sender = userService.getAUser(String.valueOf(transaction.getSid()));
        User receiver = userService.getAUser(String.valueOf(transaction.getRid()));
        if(sender==null || receiver==null) {
            logger.info("No wallet for sender or receiver");
            throw new TransactionBadRequest();
        }

        WalletInRedis redisWalletSender = walletDaoRedis.getWallet(sender.getId());
        WalletInRedis redisWalletReceiver = walletDaoRedis.getWallet(receiver.getId());
        Wallet senderWallet = walletRepository.findByUserId(sender.getId());
        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId());

        int amount = transaction.getAmount();

        if (senderWallet.getBalance() < amount) {
            throw new Exception("Not Sufficient Balance");
        }
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        redisWalletSender.setAmount(senderWallet.getBalance()-amount);
        receiverWallet.setBalance(receiverWallet.getBalance()+amount);
        redisWalletReceiver.setAmount(receiverWallet.getBalance()+amount);

        transaction.setStatus("SUCCESS");
        logger.info(String.format("$$ -> Producing Transaction --> %s", transaction));
        kafkaTemplate.send(TOPIC, Integer.toString(transaction.getId()));
        walletRepository.save(receiverWallet);
        walletRepository.save(senderWallet);

        //Updating balance in Redis Cache
        walletDaoRedis.updateWallet(redisWalletReceiver);
        walletDaoRedis.updateWallet(redisWalletSender);
        return transactionRepository.save(transaction);
    }

    @GetMapping("/getBal/{id}")
    @ApiOperation(value = "To get Balance of a user")
    int getBal(@PathVariable int id) throws Exception {
        WalletInRedis walletInRedis = null;
        walletInRedis = walletDaoRedis.getWallet(id);

        //If not present in the Redis then go for MySQL
        if(walletInRedis == null) {
            Wallet wallet = walletRepository.findByUserId(id);

            if(wallet==null) throw new Exception("Wallet not found");
            else {
                return wallet.getBalance();
            }
        }
        else {
            return walletInRedis.getAmount();
        }
    }

    @PostMapping("/addBalance")
    @ApiOperation(value = " To Add balance in users wallet ")
    AddBalanceDetails addBalance(@RequestBody AddBalanceDetails addBalanceDetails) {
        logger.info(addBalanceDetails.toString());
        Wallet wallet = walletRepository.findByUserId(addBalanceDetails.getUid());
        wallet.setBalance(addBalanceDetails.getAmount()+wallet.getBalance());
        WalletInRedis walletInRedis = walletDaoRedis.getWallet(addBalanceDetails.getUid());
        walletInRedis.setAmount(addBalanceDetails.getAmount()+wallet.getBalance());
        walletRepository.save(wallet);
        walletDaoRedis.updateWallet(walletInRedis);
        return addBalanceDetails;
    }

    @GetMapping("/txnHistory/{id}")
    @ApiOperation(value = " To create and mail a .csv file of the Transaction History ")
    String getTransactionHistory(@PathVariable int id) {
        logger.info(String.format("$$ -> Producing Transaction --> %s", id));
        String new_id = Integer.toString(id)+"txn";
        kafkaTemplate.send(TOPIC, new_id);
        return "You will receive the file on your email";
    }
}
