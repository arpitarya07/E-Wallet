package com.ewallet.WalletService.Util;

import com.ewallet.WalletService.Model.Transaction;
import com.ewallet.WalletService.Model.User;
import com.ewallet.WalletService.Repository.TransactionRepository;
import com.ewallet.WalletService.Service.EmailService;
import com.ewallet.WalletService.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionRepository transactionRepository;

    @KafkaListener(topics = "test", groupId = "group_id")
    public void consume(String id) {
        logger.info(String.format("$$ -> Consumed Message -> %s", id));

        //Checking if message is for transaction history
        if (id.contains("txn")) {
            sendTxnHistory(id);
            return;
        }
        int tid = Integer.parseInt(id);
        Optional<Transaction> transactionOptional = transactionRepository.findById(tid);
        Transaction transaction = transactionOptional.get();
        int amount = transaction.getAmount();
        User sender = userService.getAUser(String.valueOf(transaction.getSid()));
        User receiver = userService.getAUser(String.valueOf(transaction.getRid()));
        EmailService.sendEmail((sender.getEmail()));
        EmailService.sendEmail((receiver.getEmail()));
    }

    private void sendTxnHistory(String id) {
        String[] arrOfStr = id.split("t", 0);
        String newId = "";
        for (String a : arrOfStr) {
            newId = a;
            break;
        }
        int id1 = Integer.parseInt(newId);
        ArrayList<Transaction> list = (ArrayList<Transaction>) transactionRepository.findBySidAndRid(id1);
        User user1 = userService.getAUser(newId);
        String fileName = "test.csv";

        try {
            FileWriter fw = new FileWriter(fileName);

            for (Transaction transaction : list) {
                fw.append(transaction.getStatus());
                fw.append(',');
                int amt = transaction.getAmount();
                Integer obj = amt;
                fw.append(obj.toString());
                fw.append(',');
                fw.append(transaction.getDate().toString());
                fw.append(',');
                int id2 = transaction.getId();
                Integer obj2 = id2;
                fw.append(obj2.toString());
                fw.append(',');
                int rid = transaction.getRid();
                obj = rid;
                fw.append(obj.toString());
                fw.append(',');
                int sid = transaction.getSid();
                obj = sid;
                fw.append(obj.toString());
                fw.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
